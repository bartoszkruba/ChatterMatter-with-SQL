package client.clientApp.network;

import client.clientApp.Client;
import models.Message;
import models.Sendable;
import models.Encryption;

import javax.crypto.SealedObject;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;

public class Sender extends Thread {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private LinkedBlockingDeque<Sendable> outbox;
    private Encryption encrypt;


    public Sender(Socket socket) {
        this.encrypt = new Encryption();
        outbox = new LinkedBlockingDeque<>();
        this.setSocket(socket);
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        outbox.clear();
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            Client.getInstance().tryReconnect();
        }
    }

    public void sendToServer(Sendable o) {
        if (o instanceof Message) {
            outbox.add(o);
        }
    }

    private boolean hasMessagesToSend() {
        return outbox.size() > 0;
    }

    @Override
    public void run() {
        while (Client.getInstance().isRunning()) {
            while (hasMessagesToSend()) {
                while (socket.isClosed() && Client.getInstance().isRunning()) {
                    //sender waiting for reconnect
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
                try {
                    Sendable m = outbox.getFirst();
                    SealedObject encryptedObject = encrypt.encryptObject(m);
                    objectOutputStream.reset();
                    objectOutputStream.writeObject(encryptedObject);  // Try to send first sendable
                    outbox.removeFirst();               // Remove if sent
                } catch (NoSuchElementException e) {
                } catch (Exception e) {
                    Client.getInstance().tryReconnect();
                }
            }
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
