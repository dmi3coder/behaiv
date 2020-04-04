package de.dmi3y.behaiv.kernel;

import com.fasterxml.jackson.core.type.TypeReference;
import de.dmi3y.behaiv.kernel.logistic.LogisticUtils;
import de.dmi3y.behaiv.storage.BehaivStorage;
import de.dmi3y.behaiv.tools.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.ejml.simple.SimpleMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static de.dmi3y.behaiv.tools.DataMappingUtils.toDistinctListOfPairValues;
import static de.dmi3y.behaiv.tools.DataMappingUtils.toInput2dArray;

public class LogisticRegressionKernel extends Kernel {

    protected List<String> labels = new ArrayList<>();
    private Random rand;
    protected SimpleMatrix theta;

    public LogisticRegressionKernel(String id, Random rand) {
        super(id);
        this.rand = rand;
        this.partialFitAllowed = false;
    }

    public LogisticRegressionKernel(String id) {
        this(id, new Random());
    }

    @Override
    public boolean isEmpty() {
        return theta == null && data.size() == 0;
    }


    @Override
    public void fit(List<Pair<List<Double>, String>> data) {
        this.data = data;
        labels = toDistinctListOfPairValues(data);
        if (readyToPredict()) {


            //features
            double[][] inputs = toInput2dArray(data);

            //labels
            double[][] labelArray = new double[data.size()][labels.size()];
            for (int i = 0; i < data.size(); i++) {
                int dummyPos = labels.indexOf(data.get(i).getValue());
                labelArray[i][dummyPos] = 1.0;
            }

            //output layer
            final SimpleMatrix inputMatrix = new SimpleMatrix(inputs);
            final SimpleMatrix outputMatrix = new SimpleMatrix(labelArray);
            //3x4?

            //TODO dilemma on if we need to re-do theta or keep it as-is, if new features arrising we'll have a problem
            if (theta == null || (theta.numCols() != labels.size() && alwaysKeepData)) {
                theta = SimpleMatrix.random_DDRM(inputMatrix.numCols(), outputMatrix.numCols(), 0, 1, rand);
            } else if (theta.numCols() != labels.size() && !alwaysKeepData) {
                throw new UnsupportedOperationException(
                        "Partial fit of LogisticRegressionKernel is not supported. " +
                                "Number of labels differs from trained model." +
                                " Consider setting alwaysKeepData to true or changing Kernel that supports partial fit."
                );
            }

            for (int i = 0; i < 10000; i++) {
                theta = LogisticUtils.gradientDescent(inputMatrix, theta, outputMatrix, 0.1);
            }

        }

    }

    @Override
    public boolean readyToPredict() {
        return theta != null || super.readyToPredict();
    }

    @Override
    public void updateSingle(List<Double> features, String label) {
        super.updateSingle(features, label);
    }

    @Override
    public String predictOne(List<Double> features) {


        final double[] doubles = ArrayUtils.toPrimitive(features.toArray(new Double[0]));

        final SimpleMatrix inputs = new SimpleMatrix(new double[][]{doubles});

        final SimpleMatrix output = LogisticUtils.sigmoid(inputs.mult(theta));

        int maxPosition = 0;
        for (int i = 0; i < output.numCols(); i++) {
            if (output.get(0, maxPosition) < output.get(0, i)) {
                maxPosition = i;
            }
        }
        return labels.get(maxPosition);
    }

    @Override
    public void save(BehaivStorage storage) throws IOException {
        if (theta == null && (data == null || data.isEmpty())) {
            throw new IOException("Not enough data to save, network data is empty");
        }
        if(labels == null || labels.isEmpty()) {
            labels = toDistinctListOfPairValues(data);
        }
        if (labels.isEmpty()) {
            String message;
            message = "Kernel collected data but failed to get labels, couldn't save network.";
            throw new IOException(message);
        }
        if (theta == null) {
            super.save(storage);
        } else {
            if (alwaysKeepData) {
                super.save(storage);
            }
            theta.saveToFileBinary(storage.getNetworkFile(id).toString());
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(storage.getNetworkMetadataFile(id)))) {

                writer.write(objectMapper.writeValueAsString(labels));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void restore(BehaivStorage storage) throws IOException {
        boolean failedToGetTheta = false;
        try {
            theta = SimpleMatrix.loadBinary(storage.getNetworkFile(id).toString());
        } catch (IOException exception) {
            failedToGetTheta = true;
        }

        if (failedToGetTheta || alwaysKeepData) {
            super.restore(storage);
        }

        try (final BufferedReader reader = new BufferedReader(new FileReader(storage.getNetworkMetadataFile(id)))) {
            final TypeReference<List<String>> typeReference = new TypeReference<List<String>>() {
            };
            final String labelsData = reader.readLine();
            if (labelsData == null) {
                labels = new ArrayList<>();
            } else {
                labels = objectMapper.readValue(labelsData, typeReference);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
