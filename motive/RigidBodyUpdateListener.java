package motive;

/**
 * A listener interface that allows listening for movement 
 * of a rigid body within Motive
 * @author Lauren Knight
 */
public interface RigidBodyUpdateListener {
    void update(int id, float x, float y, float z);
}
