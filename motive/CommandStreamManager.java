package motive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for talking to Motive's UDP server,
 * which runs on localhost (127.0.0.1) on port 1510.
 * 
 * As frames are received by this class, any listeners listening for
 * frame updates are updated.
 * 
 * The class communicates with Motive via a socket opened on localhost::1512.
 * This port can be changed by updating the value of APPLICATION_PORT.
 * 
 * Code in the handleFrameData method was adapted from the Motive SDK
 * PythonClient sample.
 * 
 * @author Lauren Knight
 */
public class CommandStreamManager implements Runnable {
    
    // Byte order used by Motive
    private static final ByteOrder MOTIVE_PACKET_BYTE_ORDER = 
            ByteOrder.LITTLE_ENDIAN;

    // Motive's Command port (this will need to be changed 
    // if Motive's settings are changed)
    private static final int MOTIVE_COMMAND_PORT = 1510;
    // The port this application will communicate with Motive from
    private static final int APPLICATION_PORT = 1512;
    
    private DatagramSocket socket;
    private InetAddress address;
    
    // Message type sent to Motive on initial connection
    private static final short MESSAGE_CONNECT = 0;
    // Message type sent from Motive after initial connection is successful
    private static final short MESSAGE_SERVER_INFO = 1;
    // Message type sent when we receive a frame from Motive
    private static final short MESSAGE_FRAME_OF_DATA = 7;
    // Message type sent to Motive that lets it know we're still listening
    private static final short MESSAGE_KEEP_ALIVE = 10;

    // Time between keep alive messages, in milliseconds
    private static final long KEEP_ALIVE_WAIT_PERIOD = 1000; // 1000 ms = 1 second

    // the list of RigidBodyUpdateListeners that will have their update method called
    // when a frame containing at least one rigid body is received from Motive
    private List<RigidBodyUpdateListener> rigidBodyUpdateListeners;
    private List<FrameUpdateListener> frameUpdateListeners;
    
    public CommandStreamManager() {
        super();
        rigidBodyUpdateListeners = new ArrayList<>();
        frameUpdateListeners = new ArrayList<>();
        try {
            address = InetAddress.getByName("localhost");
        } catch (IOException e) {
            System.out.println("Error opening localhost Inet Address");
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Adds a RigidBodyUpdateListener to this stream manager.
     * This will cause the listener to be updated each time
     * a rigid body's location is updated within the 3D space
     * within Motive.
     * @param listener The RigidBodyUpdateListener subscribing to updates.
     */
    public void addRigidBodyUpdateListener(RigidBodyUpdateListener listener) {
        rigidBodyUpdateListeners.add(listener);
    }

    /**
     * Adds a FrameUpdateListener to this stream manager.
     * This will cause the listener to be updated each time
     * a frame is received from Motive.
     * @param listener The subscribing listener
     */
    public void addFrameUpdateListener(FrameUpdateListener listener) {
        frameUpdateListeners.add(listener);
    }
    
    /**
     * Sends a 'keep alive' signal to Motive, which tells Motive
     * that we're still listening for packets.
     */
    private void sendKeepAliveSignal() {
        byte[] buffer = new byte[5];
        ByteBuffer wrapper = ByteBuffer.wrap(buffer).order(
                MOTIVE_PACKET_BYTE_ORDER);
        try {
            DatagramPacket sent = new DatagramPacket(buffer, buffer.length, 
                    address, MOTIVE_COMMAND_PORT);
            wrapper.putShort(MESSAGE_KEEP_ALIVE);
                socket.send(sent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A new Thread is created within the run() method.
     * This method contains a loop which will call sendKeepAliveSignal
     * once every KEEP_ALIVE_WAIT_PERIOD milliseconds.
     */
    private void keepAliveDaemon() {
        while (true) {
            try {
                Thread.sleep(KEEP_ALIVE_WAIT_PERIOD);
            } catch (InterruptedException e) {}
            sendKeepAliveSignal();
        }
    }

    /**
     * This method turns the packet byte data into readable, usable data
     * It also updates the stream manager's rigid body listeners
     * (This is what drives the animation of the panel)
     
     * Note: this method works with Motive version 2.1.1

     * @param buffer a ByteBuffer passed by run()
     */
    private void handleFrameDataV2_1_1(ByteBuffer buffer) {
        short bufferSize = buffer.getShort();
        // dumpBuffer(buffer.array(), bufferSize);
        int frameNumber = buffer.getInt();
        final int markerSetCount = buffer.getInt();
        // System.out.printf("buffer size: %d\nframe no: %d\nmarket set count: %d\n",
        //         bufferSize, frameNumber, markerSetCount);
        // List<String> modelNames = new ArrayList<>();
        for (int markerSet = 0; markerSet < markerSetCount; markerSet++) {
            byte c = 1;
            int offset = buffer.position();
            int size = 0;
            while (c != 0) {
                c = buffer.get();
                size++;
            }
            // try {
            //     System.out.println(new String(buffer.array(), offset, size, 
            //             "UTF-8"));
            // } catch (UnsupportedEncodingException ex) {}
            int markerCount = buffer.getInt();
            for (int marker = 0; marker < markerCount; marker++) {
                float x = buffer.getFloat();
                float y = buffer.getFloat();
                float z = buffer.getFloat();
                // System.out.printf("%d %.2f %.2f %.2f\n", marker, x, y, z);
            }
        }
        final int unlabeledMarkerCount = buffer.getInt();
        // System.out.printf("ulmarker count: %d\n", unlabeledMarkerCount);
        for (int marker = 0; marker < unlabeledMarkerCount; marker++) {
            float x = buffer.getFloat();
            float y = buffer.getFloat();
            float z = buffer.getFloat();
        }

        final int rigidBodyCount = buffer.getInt();
        // System.out.printf("RB count: %d\n", rigidBodyCount);
        for (int body = 0; body < rigidBodyCount; body++) {
            // id (this will come into play when we have multiple bodies)
            int bodyID = buffer.getInt();
            // coordinates of the rigid body (what we wanted!)
            float x = buffer.getFloat();
            float y = buffer.getFloat();
            float z = buffer.getFloat();
            // System.out.printf("id: %d %.2f %.2f %.2f\n", bodyID, x, y, z);
            for (RigidBodyUpdateListener listener : rigidBodyUpdateListeners) {
                if (listener != null)
                    listener.update(bodyID, x, y, z);
            }
            float[] quaternions = new float[4];
            // unneeded rotational information
            quaternions[0] = buffer.getFloat();
            quaternions[1] = buffer.getFloat();
            quaternions[2] = buffer.getFloat();
            quaternions[3] = buffer.getFloat();

            // System.out.printf("rb id %d: %.2f, %.2f, %.2f ... %.2f, %.2f, %.2f, %.2f\n",
            //         bodyID, x, y, z, quaternions[0], quaternions[1], quaternions[2], quaternions[3]);

            // get rid of junk in the way
            buffer.getFloat();
            buffer.getShort();

            // determine what direction the body is facing based on the quaternions
            // this is strictly the rotation along the Z axis
            // thanks to https://automaticaddison.com/how-to-convert-a-quaternion-to-a-rotation-matrix/
            // float forwardX = 2 * (quaternions[1] * quaternions[2] + quaternions[0] * quaternions[3]);
            // float forwardY = 2 * (quaternions[0] * quaternions[0] + quaternions[2] * quaternions[2]) - 1;
            // float distance = (float) Math.sqrt(forwardX * forwardX + forwardY * forwardY);
            // if (distance == 0) {
            //     distance = 1;
            // }

            // forwardX /= distance;
            // forwardY /= distance;
            
            /*final int rbMarkerCount = buffer.getInt();
            for (int rbMarker = 0; rbMarker < rbMarkerCount; rbMarker++) {
                float markerX = buffer.getFloat();
                float markerY = buffer.getFloat();
                float markerZ = buffer.getFloat();
            }
            System.out.printf("rbMarkerCount: %d\n", rbMarkerCount);
            
            for (int rbMarker = 0; rbMarker < rbMarkerCount; rbMarker++) {
                int markerID = buffer.getInt();
            }

            for (int rbMarker = 0; rbMarker < rbMarkerCount; rbMarker++) {
                float markerSize = buffer.getFloat();
                System.out.printf("Marker size: %.2f\n");
            }*/

            // float markerError = buffer.getFloat();
        }
        // below is NOT WORKING... but may not be needed ;)
        // int skeletonCount = buffer.getInt();
        // for (int skeleton = 0; skeleton < skeletonCount; skeleton++) {
        //     int skeletonID = buffer.getInt();
        //     int skeletonRigidBodyCount = buffer.getInt();
        //     for (int body = 0; body < skeletonRigidBodyCount; body++) {
        //         int bodyID = buffer.getInt();
        //         float x = buffer.getFloat();
        //         float y = buffer.getFloat();
        //         float z = buffer.getFloat();
        //         System.out.printf("body %d location: %.2f, %.2f, %.2f\n", 
        //                 bodyID, x, y, z);
        //         float orientationX = buffer.getFloat();
        //         float orientationY = buffer.getFloat();
        //         float orientationZ = buffer.getFloat();
        //         int bodyMarkerCount = buffer.getInt();
        //         for (int marker = 0; marker < bodyMarkerCount; marker++) {
        //             float markerX = buffer.getFloat();
        //             float markerY = buffer.getFloat();
        //             float markerZ = buffer.getFloat();
        //         }
        //     }
        // }
        for (FrameUpdateListener listener : frameUpdateListeners) {
            listener.update();
        }
    }
    
    /**
     * This method turns the packet byte data into readable, usable data
     * It also updates the stream manager's rigid body listeners
     * (This is what drives the animation of the panel)
     
     * Note: this method works with Motive versions 3 or higher

     * @param buffer a ByteBuffer passed by run()
     */
    private void handleFrameDataV3(ByteBuffer buffer) {
        short bufferSize = buffer.getShort();
        int frameNumber = buffer.getInt();
        int markerSetCount = buffer.getInt();
        // List<String> modelNames = new ArrayList<>();
        for (int markerSet = 0; markerSet < markerSetCount; markerSet++) {
            byte c = 1;
            int offset = buffer.position();
            int size = 0;
            while (c != 0) {
                c = buffer.get();
                size++;
            }
            // try {
            //     System.out.println(new String(buffer.array(), offset, size, 
            //             "UTF-8"));
            // } catch (UnsupportedEncodingException ex) {}
            int markerCount = buffer.getInt();
            for (int marker = 0; marker < markerCount; marker++) {
                float x = buffer.getFloat();
                float y = buffer.getFloat();
                float z = buffer.getFloat();
                // System.out.printf("%d %.2f %.2f %.2f\n", marker, x, y, z);
            }
        }
        int unlabeledMarkerCount = buffer.getInt();
        for (int marker = 0; marker < unlabeledMarkerCount; marker++) {
            float x = buffer.getFloat();
            float y = buffer.getFloat();
            float z = buffer.getFloat();
        }
        int rigidBodyCount = buffer.getInt();
        for (int body = 0; body < rigidBodyCount; body++) {
            // id (this will come into play when we have multiple bodies)
            int bodyID = buffer.getInt();
            // coordinates of the rigid body (what we wanted!)
            float x = buffer.getFloat();
            float y = buffer.getFloat();
            float z = buffer.getFloat();
            // System.out.printf("id: %d %.2f %.2f %.2f\n", bodyID, x, y, z);
            for (RigidBodyUpdateListener listener : rigidBodyUpdateListeners) {
                if (listener != null)
                    listener.update(bodyID, x, y, z);
            }
            float[] quaternions = new float[4];
            // unneeded rotational information
            quaternions[0] = buffer.getFloat();
            quaternions[1] = buffer.getFloat();
            quaternions[2] = buffer.getFloat();
            quaternions[3] = buffer.getFloat();

            // determine what direction the body is facing based on the quaternions
            // this is strictly the rotation along the Z axis
            // thanks to https://automaticaddison.com/how-to-convert-a-quaternion-to-a-rotation-matrix/
            // float forwardX = 2 * (quaternions[1] * quaternions[2] + quaternions[0] * quaternions[3]);
            // float forwardY = 2 * (quaternions[0] * quaternions[0] + quaternions[2] * quaternions[2]) - 1;
            // float distance = (float) Math.sqrt(forwardX * forwardX + forwardY * forwardY);
            // if (distance == 0) {
            //     distance = 1;
            // }

            // forwardX /= distance;
            // forwardY /= distance;
            
            float markerError = buffer.getFloat();
            byte byteA = buffer.get();
            byte byteB = buffer.get();
            boolean trackingValid = (byteA & 0x01) != 0;
        }
        // below is NOT WORKING... but may not be needed ;)
        // int skeletonCount = buffer.getInt();
        // for (int skeleton = 0; skeleton < skeletonCount; skeleton++) {
        //     int skeletonID = buffer.getInt();
        //     int skeletonRigidBodyCount = buffer.getInt();
        //     for (int body = 0; body < skeletonRigidBodyCount; body++) {
        //         int bodyID = buffer.getInt();
        //         float x = buffer.getFloat();
        //         float y = buffer.getFloat();
        //         float z = buffer.getFloat();
        //         System.out.printf("body %d location: %.2f, %.2f, %.2f\n", 
        //                 bodyID, x, y, z);
        //         float orientationX = buffer.getFloat();
        //         float orientationY = buffer.getFloat();
        //         float orientationZ = buffer.getFloat();
        //         int bodyMarkerCount = buffer.getInt();
        //         for (int marker = 0; marker < bodyMarkerCount; marker++) {
        //             float markerX = buffer.getFloat();
        //             float markerY = buffer.getFloat();
        //             float markerZ = buffer.getFloat();
        //         }
        //     }
        // }
        for (FrameUpdateListener listener : frameUpdateListeners) {
            listener.update();
        }
    }

    /**
     * This method turns the packet byte data into readable, usable data
     * It also updates the stream manager's rigid body listeners
     * (This is what drives the animation of the panel)
     * 
     * Note: this version works with Motive version 1.10.2 only.

     * @param buffer a ByteBuffer passed by run()
     */
    private void handleFrameDataV1_10_2(ByteBuffer buffer) {
        short bufferSize = buffer.getShort();
        int frameNumber = buffer.getInt();
        final int markerSetCount = buffer.getInt();
        // List<String> modelNames = new ArrayList<>();
        for (int markerSet = 0; markerSet < markerSetCount; markerSet++) {
            byte c = 1;
            int offset = buffer.position();
            int size = 0;
            while (c != 0) {
                c = buffer.get();
                size++;
            }
            // try {
            //     System.out.println(new String(buffer.array(), offset, size, 
            //             "UTF-8"));
            // } catch (UnsupportedEncodingException ex) {}
            final int markerCount = buffer.getInt();
            for (int marker = 0; marker < markerCount; marker++) {
                float x = buffer.getFloat();
                float y = buffer.getFloat();
                float z = buffer.getFloat();
                // System.out.printf("%d %.2f %.2f %.2f\n", marker, x, y, z);
            }
        }
        final int unlabeledMarkerCount = buffer.getInt();
        for (int marker = 0; marker < unlabeledMarkerCount; marker++) {
            float x = buffer.getFloat();
            float y = buffer.getFloat();
            float z = buffer.getFloat();
        }
        final int rigidBodyCount = buffer.getInt();
        for (int body = 0; body < rigidBodyCount; body++) {
            //System.out.println(body);
            // id (this will come into play when we have multiple bodies)
            int bodyID = buffer.getInt();
            System.out.println(bodyID);
            //System.out.println("body id: " + bodyID);
            // coordinates of the rigid body (what we wanted!)
            float x = buffer.getFloat();
            float y = buffer.getFloat();
            float z = buffer.getFloat();
            // System.out.printf("id: %d %.2f %.2f %.2f\n", bodyID, x, y, z);
            for (RigidBodyUpdateListener listener : rigidBodyUpdateListeners) {
                listener.update(bodyID, x, y, z);
            }
            float[] quaternions = new float[4];
            // unneeded rotational information
            quaternions[0] = buffer.getFloat();
            quaternions[1] = buffer.getFloat();
            quaternions[2] = buffer.getFloat();
            quaternions[3] = buffer.getFloat();

            // determine what direction the body is facing based on the quaternions
            // this is strictly the rotation along the Z axis
            // thanks to https://automaticaddison.com/how-to-convert-a-quaternion-to-a-rotation-matrix/
            // float forwardX = 2 * (quaternions[1] * quaternions[2] + quaternions[0] * quaternions[3]);
            // float forwardY = 2 * (quaternions[0] * quaternions[0] + quaternions[2] * quaternions[2]) - 1;
            // float distance = (float) Math.sqrt(forwardX * forwardX + forwardY * forwardY);
            // if (distance == 0) {
            //     distance = 1;
            // }

            // forwardX /= distance;
            // forwardY /= distance;
            
            final int rbMarkerCount = buffer.getInt();
            for (int rbMarker = 0; rbMarker < rbMarkerCount; rbMarker++) {
                float markerX = buffer.getFloat();
                float markerY = buffer.getFloat();
                float markerZ = buffer.getFloat();
            }
        }
        // below is NOT WORKING... but may not be needed ;)
        // int skeletonCount = buffer.getInt();
        // for (int skeleton = 0; skeleton < skeletonCount; skeleton++) {
        //     int skeletonID = buffer.getInt();
        //     int skeletonRigidBodyCount = buffer.getInt();
        //     for (int body = 0; body < skeletonRigidBodyCount; body++) {
        //         int bodyID = buffer.getInt();
        //         float x = buffer.getFloat();
        //         float y = buffer.getFloat();
        //         float z = buffer.getFloat();
        //         System.out.printf("body %d location: %.2f, %.2f, %.2f\n", 
        //                 bodyID, x, y, z);
        //         float orientationX = buffer.getFloat();
        //         float orientationY = buffer.getFloat();
        //         float orientationZ = buffer.getFloat();
        //         int bodyMarkerCount = buffer.getInt();
        //         for (int marker = 0; marker < bodyMarkerCount; marker++) {
        //             float markerX = buffer.getFloat();
        //             float markerY = buffer.getFloat();
        //             float markerZ = buffer.getFloat();
        //         }
        //     }
        // }
        for (FrameUpdateListener listener : frameUpdateListeners) {
            listener.update();
        }
    }
    
    @Override
    public void run() {
        try {
            // Start with an unbound socket
            socket = new DatagramSocket(APPLICATION_PORT, address);
            // create a 64K byte buffer
            byte[] buffer = new byte[64 * 1024];
            // Create a packet which will be sent to Motive
            DatagramPacket sent = new DatagramPacket(buffer, 2, address, MOTIVE_COMMAND_PORT);
            // Send the packet (size of 2 bytes, both bytes 0) to Motive
            // These two zero bytes indicate the MESSAGE_CONNECT signal,
            // causing Motive to begin sending us frame data (yay)
            socket.send(sent);
            // Create a packet data structure that will be used to 
            // receive packets from Motive
            DatagramPacket received = new DatagramPacket(buffer, buffer.length,
                    sent.getAddress(), sent.getPort());
            ByteBuffer wrapper = ByteBuffer.wrap(buffer).order(
                    MOTIVE_PACKET_BYTE_ORDER);

            // Create a background thread that will send Motive a keep alive signal
            // once every KEEP_ALIVE_WAIT_PERIOD milliseconds 
            // (this maintains the connection to Motive)
            new Thread(this::keepAliveDaemon).start();

            // Continuously receive packets from Motive
            while (true) {
                // Block thread until packet received
                socket.receive(received);
                // Determine packet type
                short messageType = wrapper.getShort();
                switch (messageType) {
                    case MESSAGE_SERVER_INFO:
                        // This only happens once, on initial connection
                        System.out.println("Successfully connected to "
                                + "command server!");
                        break;
                    case MESSAGE_FRAME_OF_DATA:
                        // This case occurs roughly 60-120 times/second

                        // Method call breaks down the packet into useful data
                        // and updates the manager's listeners with this new data 
                        handleFrameDataV2_1_1(wrapper);
                        break;
                    default:
                        // do nothing; we don't care about other messages
                }
                // reset the wrapper to the first byte in the buffer
                wrapper.rewind();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dumpBuffer(final byte[] array, final int messageLength) {
        for (int i = 0; i < messageLength; i++) {
            System.out.printf("%02X ", array[i]);
            if (i % 0x10 == 0xF) {
                System.out.println();
            }
        }
        if (messageLength % 0x10 != 0) {
            System.out.println();
        }
        System.out.println("- - - - - - - - - - - - - - - - - - - -");
    }
    
}
