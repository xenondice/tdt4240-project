package tdt4240.lyvlyvtjuesyv;

/**
 * Created by RayTM on 29.03.2016.
 */
public class ClientListner implements Runnable {

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            System.out.println("working!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
