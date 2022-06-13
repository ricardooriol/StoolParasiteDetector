package com.Activities;

import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;

import java.util.List;
import java.util.Objects;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;


@SuppressWarnings("ALL")
public class TensorflowImageProcessor {

    private ManagedChannel channel;
    private PredictionServiceGrpc.PredictionServiceBlockingStub stub;
    private int numParasites = 0;
    private final float sensitivity = 0.5f;
    private final MainActivity mainActivity = new MainActivity();

    public Object[] processImage(Object[] results) {
        if (mainActivity.internetConnection()) {
            try {
                Predict.PredictRequest request = createGRPCRequest((Bitmap) results[0]);
                Predict.PredictResponse response = stub.predict(request);
                postProcessGRPCResponse(response, results);
                channel.shutdownNow();
            } catch (Exception e) {
                //THERE WAS AN UNEXPECTED ERROR
                e.printStackTrace();
                results[1] = 0;
                channel.shutdownNow();
                return results;
            }
        } else if (!mainActivity.internetConnection()) {
            //THERE IS NO INTERNET CONNECTION
            results[1] = 3;
            channel.shutdownNow();
            return results;
        }
        if (numParasites == 0) {
            //NO PARASITES WERE DETECTED
            results[1] = 2;
        } else {
            //IMAGE PROCESSED SUCCESSFULLY
            results[1] = 1;
        }
        channel.shutdownNow();
        return results;
    }

    private Predict.PredictRequest createGRPCRequest(Bitmap bitmap) {
        String host = "3.128.144.86";
        int port = 8500;
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        if (stub == null) {
            stub = PredictionServiceGrpc.newBlockingStub(channel);
        }

        Model.ModelSpec.Builder modelSpecBuilder = Model.ModelSpec.newBuilder();
        modelSpecBuilder.setName("parasite_model");
        modelSpecBuilder.setSignatureName("serving_default");

        Predict.PredictRequest.Builder builder = Predict.PredictRequest.newBuilder();
        builder.setModelSpec(modelSpecBuilder);

        int INPUT_IMG_HEIGHT = bitmap.getHeight();
        int INPUT_IMG_WIDTH = bitmap.getWidth();

        TensorProto.Builder tensorProtoBuilder = TensorProto.newBuilder();
        tensorProtoBuilder.setDtype(DataType.DT_UINT8);

        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(INPUT_IMG_HEIGHT));
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(INPUT_IMG_WIDTH));
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(3));
        tensorProtoBuilder.setTensorShape(tensorShapeBuilder.build());

        int[] inputImg = new int[INPUT_IMG_HEIGHT * INPUT_IMG_WIDTH];
        bitmap.getPixels(inputImg, 0, INPUT_IMG_WIDTH, 0, 0, INPUT_IMG_WIDTH, INPUT_IMG_HEIGHT);
        int pixel;
        for (int i = 0; i < INPUT_IMG_HEIGHT; i++) {
            for (int j = 0; j < INPUT_IMG_WIDTH; j++) {
                // EXTRACT RGB VALUES FROM EACH PIXEL
                pixel = inputImg[i * INPUT_IMG_WIDTH + j];
                tensorProtoBuilder.addIntVal((pixel >> 16) & 0xff);
                tensorProtoBuilder.addIntVal((pixel >> 8) & 0xff);
                tensorProtoBuilder.addIntVal((pixel) & 0xff);
            }
        }
        TensorProto tensorProto = tensorProtoBuilder.build();
        builder.putInputs("input_tensor", tensorProto);
        builder.addOutputFilter("num_detections");
        builder.addOutputFilter("detection_boxes");
        builder.addOutputFilter("detection_classes");
        builder.addOutputFilter("detection_scores");
        return builder.build();
    }

    private void postProcessGRPCResponse(Predict.PredictResponse response, Object[] results) {
        try {
            List<Float> detectionBoxes = Objects.requireNonNull(response.getOutputsMap().get("detection_boxes")).getFloatValList();
            List<Float> detectionScores = Objects.requireNonNull(response.getOutputsMap().get("detection_scores")).getFloatValList();
            float numDetections = Objects.requireNonNull(response.getOutputsMap().get("num_detections")).getFloatValList().get(0);
            for (int i = 0; i < numDetections; i++) {
                if (detectionScores.get(i) < sensitivity) break;
                float ymin = detectionBoxes.get(i * 4);
                float xmin = detectionBoxes.get(i * 4 + 1);
                float ymax = detectionBoxes.get(i * 4 + 2);
                float xmax = detectionBoxes.get(i * 4 + 3);
                List<Float> detectionClasses = Objects.requireNonNull(response.getOutputsMap().get("detection_classes")).getFloatValList();
                float detectionClass = detectionClasses.get(i);
                int parasiteID = (int) detectionClass;
                String parasiteName = getParasiteName(parasiteID);
                results[0] = displayResult(results, ymin, xmin, ymax, xmax, parasiteName);
                numParasites++;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    private String getParasiteName(int parasiteID) {
        String parasiteName = null;
        try {
            String JSON = JSONFileHandler.readJSON(mainActivity.getContext());
            JSONObject object = new JSONObject(JSON);
            JSONArray jsonArray = object.getJSONArray("parasites");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject parasite = jsonArray.getJSONObject(i);

                int id = (Integer) parasite.get("id");
                if (id == parasiteID) {
                    parasiteName = (String) parasite.get("display_name");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parasiteName;
    }

    private Bitmap displayResult(Object[] results, float ymin, float xmin, float ymax, float xmax, String parasiteName) {
        Bitmap inputImgBitmap = (Bitmap) results[0];
        int INPUT_IMG_HEIGHT = inputImgBitmap.getHeight();
        int INPUT_IMG_WIDTH = inputImgBitmap.getWidth();
        float left = xmin * INPUT_IMG_WIDTH;
        float right = xmax * INPUT_IMG_WIDTH;
        float top = ymin * INPUT_IMG_HEIGHT;
        float bottom = ymax * INPUT_IMG_HEIGHT;
        Bitmap resultInputBitmap = inputImgBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultInputBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setColor(RED);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setStrokeWidth(4);
        paint.setColor(WHITE);
        paint.setTextSize(60);
        canvas.drawText(parasiteName, left, top, paint);
        numParasites++;
        return resultInputBitmap;
    }
}