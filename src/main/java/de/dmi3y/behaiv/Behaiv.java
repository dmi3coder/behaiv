/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package de.dmi3y.behaiv;

import de.dmi3y.behaiv.kernel.Kernel;
import de.dmi3y.behaiv.node.ActionableNode;
import de.dmi3y.behaiv.node.BehaivNode;
import de.dmi3y.behaiv.provider.Provider;
import de.dmi3y.behaiv.provider.ProviderCallback;
import de.dmi3y.behaiv.session.CaptureSession;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import org.apache.commons.math3.util.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Behaiv implements ProviderCallback {

    private static ReplaySubject<String> subject;
    private Kernel kernel;
    private List<Provider> providers;
    private CaptureSession currentSession;
    private boolean predict = true;

    private Behaiv() {
        providers = new ArrayList<>();

    }

    public synchronized static Behaiv with(@Nonnull Kernel kernel) {
        Behaiv behaiv = new Behaiv();
        behaiv.kernel = kernel;
        subject = ReplaySubject.create();
        return behaiv;

    }

    public Behaiv setKernel(@Nonnull Kernel kernel) {
        this.kernel = kernel;
        return this;
    }

    public Behaiv setProvider(@Nonnull Provider provider) {
        providers.add(provider);
        return this;
    }

    public Behaiv setBehaivNode() {
        return this;
    }

    public Behaiv register(@Nonnull BehaivNode node, @Nullable String name) {
        if (node instanceof ActionableNode) {
            currentSession.captureLabel(name);
        }
        return this;
    }

    public Observable<String> register(@Nonnull BehaivNode node) {
        this.register(node, null);
        return subject;
    }

    public void startCapturing(boolean predict) {
        this.predict = predict;
        currentSession = new CaptureSession(providers);
        currentSession.start(this);
    }

    @Override
    public void onFeaturesCaptured(List<Pair<Double, String>> features) {
        if (kernel.readyToPredict() && predict) {
            subject.onNext(kernel.predictOne(features.stream().map(Pair::getFirst).collect(Collectors.toCollection(ArrayList::new))));
        }
    }

    public void stopCapturing(boolean discard) {
        if (discard) {
            currentSession = null;
            return;
        }
        String label = currentSession.getLabel();
        List<Pair<Double, String>> features = currentSession.getFeatures();
        kernel.updateSingle(features.stream().map(Pair::getFirst).collect(Collectors.toCollection(ArrayList::new)), label);

    }
}
