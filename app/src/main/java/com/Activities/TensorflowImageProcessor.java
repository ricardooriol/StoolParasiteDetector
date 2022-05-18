package com.Activities;

import static android.graphics.Color.RED;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;

import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

public class TensorflowImageProcessor {

    private PredictionServiceGrpc.PredictionServiceBlockingStub stub;

    public Object[] processImage(Object[] results) {
        try {
            Predict.PredictRequest request = createGRPCRequest((Bitmap) results[0]);
            Predict.PredictResponse response = stub.predict(request);
            postProcessGRPCResponse(response, results);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private Predict.PredictRequest createGRPCRequest(Bitmap bitmap) {
        String host = "3.128.144.86";
        int port = 8500;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
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
                // Extract RBG values from each pixel; alpha is ignored.
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
            int maxIndex = 0;
            List<Float> detectionBoxes = response.getOutputsMap().get("detection_boxes").getFloatValList();
            List<Float> detectionScores = response.getOutputsMap().get("detection_scores").getFloatValList();
            float numDetections = response.getOutputsMap().get("num_detections").getFloatValList().get(0);
            System.out.println(numDetections);
            for (int j = 0; j < numDetections; j++) {
                maxIndex = detectionScores.get(j) > detectionScores.get(maxIndex + 1) ? j : maxIndex;
            }
            Float detectionClasses = response.getOutputsMap().get("detection_classes").getFloatValList().get(maxIndex);
            System.out.println(detectionClasses);
            float ymin = detectionBoxes.get(maxIndex * 4);
            float xmin = detectionBoxes.get(maxIndex * 4 + 1);
            float ymax = detectionBoxes.get(maxIndex * 4 + 2);
            float xmax = detectionBoxes.get(maxIndex * 4 + 3);
            displayResult(results, ymin, xmin, ymax, xmax);
        } catch (NullPointerException npe) {
            results[1] = false;
        }
        results[1] = true;
    }


    private void displayResult(Object[] results, float ymin, float xmin, float ymax, float xmax) {
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
    }

}