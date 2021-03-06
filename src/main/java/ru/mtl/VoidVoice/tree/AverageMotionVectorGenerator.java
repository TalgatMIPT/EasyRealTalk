package ru.mtl.VoidVoice.tree;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Hand;
import ru.mtl.VoidVoice.model.*;
import ru.mtl.VoidVoice.utils.MotionVectorTouchesConverter;
import ru.mtl.VoidVoice.worker.Presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class AverageMotionVectorGenerator {

    private final static int AVERAGE_FRAME_NUMBER = 10;
    private static int frames_number = 0;
    private List<Frame> frames;

    private MotionVector resMotionVector;

    private Presenter presenter;

    private Vector leftPalmNormal;
    private Vector rightPalmNormal;
    private Vector leftHandDirection;
    private Vector rightHandDirection;
    private int leftHandsNumber = 0;
    private int rightHandsNumber = 0;
    private float leftConfidence = 0;
    private float rightConfidence = 0;
    private Vector leftHandVelocity;
    private Vector rightHandVelocity;

    private Vector leftPinkyDir;
    private Vector leftRingDir;
    private Vector leftMiddleDir;
    private Vector leftIndexDir;
    private Vector leftThumbDir;

    private Vector rightPinkyDir;
    private Vector rightRingDir;
    private Vector rightMiddleDir;
    private Vector rightIndexDir;
    private Vector rightThumbDir;

    private Vector leftPinkyPos;
    private Vector leftRingPos;
    private Vector leftMiddlePos;
    private Vector leftIndexPos;
    private Vector leftThumbPos;

    private Vector rightPinkyPos;
    private Vector rightRingPos;
    private Vector rightMiddlePos;
    private Vector rightIndexPos;
    private Vector rightThumbPos;

    private ReentrantLock lock;

    public AverageMotionVectorGenerator(Presenter presenter) {
        frames = new ArrayList<>();
        frames_number = 0;
        initNumbers();
        initVectors();
        lock = new ReentrantLock();
        this.presenter = presenter;
    }

    private void initNumbers() {
        frames_number = 0;
        leftHandsNumber = 0;
        rightHandsNumber = 0;
        leftConfidence = 0;
        rightConfidence = 0;
    }

    private void initVectors() {
        leftPalmNormal = new Vector(0, 0, 0);
        rightPalmNormal = new Vector(0, 0, 0);

        leftHandDirection = new Vector(0, 0, 0);
        rightHandDirection = new Vector(0, 0, 0);

        leftHandVelocity = new Vector(0, 0, 0);
        rightHandVelocity = new Vector(0, 0, 0);

        leftPinkyDir = new Vector(0, 0, 0);
        leftRingDir = new Vector(0, 0, 0);
        leftMiddleDir = new Vector(0, 0, 0);
        leftIndexDir = new Vector(0, 0, 0);
        leftThumbDir = new Vector(0, 0, 0);

        rightPinkyDir = new Vector(0, 0, 0);
        rightRingDir = new Vector(0, 0, 0);
        rightMiddleDir = new Vector(0, 0, 0);
        rightIndexDir = new Vector(0, 0, 0);
        rightThumbDir = new Vector(0, 0, 0);

        leftPinkyPos = new Vector(0, 0, 0);
        leftRingPos = new Vector(0, 0, 0);
        leftMiddlePos = new Vector(0, 0, 0);
        leftIndexPos = new Vector(0, 0, 0);
        leftThumbPos = new Vector(0, 0, 0);

        rightPinkyPos = new Vector(0, 0, 0);
        rightRingPos = new Vector(0, 0, 0);
        rightMiddlePos = new Vector(0, 0, 0);
        rightIndexPos = new Vector(0, 0, 0);
        rightThumbPos = new Vector(0, 0, 0);
    }

    public void addFrame(Frame frame) {
        lock.lock();
        frames.add(frame);
        ++frames_number;
        if (frames_number >= AVERAGE_FRAME_NUMBER) {
            try {
                generate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            handsNuller();
            frames_number = 0;
            initNumbers();
            initVectors();
            frames = new ArrayList<>();
            presenter.motionVectorHandler(resMotionVector);
        }
        lock.unlock();
    }


    private void generate() {
        //Get hands
        if (frames_number == 0) {
            return;
        }
        for (Frame frame : frames) {
            for (Hand hand : frame.hands()) {
                if (!hand.equals(Hand.invalid())) {
                    if (hand.isLeft()) {
                        ++leftHandsNumber;
                        leftPalmNormal = coordinateSummer(leftPalmNormal, hand.palmNormal());
                        leftHandDirection = coordinateSummer(leftHandDirection, hand.direction());
                        leftConfidence += hand.confidence();
                        leftHandVelocity = coordinateSummer(leftHandVelocity, hand.palmVelocity());
                        plusLeftFingers(hand);
                    } else if (hand.isRight()) {
                        ++rightHandsNumber;
                        rightPalmNormal = coordinateSummer(rightPalmNormal, hand.palmNormal());
                        rightHandDirection = coordinateSummer(rightHandDirection, hand.direction());
                        rightConfidence += hand.confidence();
                        rightHandVelocity = coordinateSummer(rightHandVelocity, hand.palmVelocity());
                        plusRightFingers(hand);
                    }
                }
            }
        }
        averageHands();
        setAverageMotionVector();
    }

    public MotionVector getResMotionVector() {
        return resMotionVector;
    }

    /*///////////////////////////////////////////////////////////////////////////////////////////////////////////////*/

    private void setAverageMotionVector() {
        MotionVector res = new MotionVector();

        ru.mtl.VoidVoice.model.Hand leftHand = new ru.mtl.VoidVoice.model.Hand();
        ru.mtl.VoidVoice.model.Hand rightHand = new ru.mtl.VoidVoice.model.Hand();

        leftHand.setConfidence(leftConfidence);
        leftHand.setPalmDirectionVector(new Vector3d(leftHandDirection));
        leftHand.setPalmNormalVector(new Vector3d(leftPalmNormal));
        leftHand.setPalmVelocity(new Vector3d(leftHandVelocity));

        rightHand.setConfidence(rightConfidence);
        rightHand.setPalmDirectionVector(new Vector3d(rightHandDirection));
        rightHand.setPalmNormalVector(new Vector3d(rightPalmNormal));
        rightHand.setPalmVelocity(new Vector3d(rightHandVelocity));

        res.setLeftHand(leftHand);
        res.setRightHand(rightHand);

        res.setLeftFingersList(generateLeftFingerList());
        res.setRightFingersList(generateRightFingerList());

        try {
            res.setTouchList(MotionVectorTouchesConverter.createTouchList(resMotionVector));
        } catch (IllegalArgumentException e) {
            res.setTouchList(new ArrayList<>());
        }

        res.setLeftHandMotion(null);
        res.setRightHandMotion(null);
        resMotionVector = res;
    }

    private void addToFingerList(List<ru.mtl.VoidVoice.model.Finger> list, FingerType type, Vector dir,
                                 Vector palmNormal, Vector pos) {
        ru.mtl.VoidVoice.model.Finger tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(type);
        tmp.setFingerDirectionVector(new Vector3d(dir));
        tmp.setCurvature(palmNormal, dir);
        tmp.setFingerTipPosition(new Point3d(pos));
        list.add(tmp);
    }

    private List<ru.mtl.VoidVoice.model.Finger> generateLeftFingerList() {
/*
        List<ru.mtl.VoidVoice.model.Finger> res = new ArrayList<>();
        ru.mtl.VoidVoice.model.Finger tmp = new ru.mtl.VoidVoice.model.Finger();

        tmp.setFingerType(FingerType.Thumb);
        tmp.setFingerDirectionVector(new Vector3d(leftThumbDir));
        tmp.setCurvature(leftPalmNormal, leftThumbDir);
        tmp.setFingerTipPosition(new Point3d(leftThumbPos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Index);
        tmp.setFingerDirectionVector(new Vector3d(leftIndexDir));
        tmp.setCurvature(leftPalmNormal, leftIndexDir);
        tmp.setFingerTipPosition(new Point3d(leftIndexPos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Middle);
        tmp.setFingerDirectionVector(new Vector3d(leftMiddleDir));
        tmp.setCurvature(leftPalmNormal, leftMiddleDir);
        tmp.setFingerTipPosition(new Point3d(leftMiddlePos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Ring);
        tmp.setFingerDirectionVector(new Vector3d(leftRingDir));
        tmp.setCurvature(leftPalmNormal, leftRingDir);
        tmp.setFingerTipPosition(new Point3d(leftMiddlePos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Pinky);
        tmp.setFingerDirectionVector(new Vector3d(leftPinkyDir));
        tmp.setCurvature(leftPalmNormal, leftPinkyDir);
        tmp.setFingerTipPosition(new Point3d(leftPinkyPos));
        res.add(tmp);
*/
        List<ru.mtl.VoidVoice.model.Finger> res = new ArrayList<>();
        addToFingerList(res, FingerType.Thumb, leftThumbDir, leftPalmNormal, leftThumbPos);
        addToFingerList(res, FingerType.Index, leftIndexDir, leftPalmNormal, leftIndexPos);
        addToFingerList(res, FingerType.Middle, leftMiddleDir, leftPalmNormal, leftMiddlePos);
        addToFingerList(res, FingerType.Ring, leftRingDir, leftPalmNormal, leftRingPos);
        addToFingerList(res, FingerType.Pinky, leftPinkyDir, leftPalmNormal, leftPinkyPos);
        return res;
    }


    private List<ru.mtl.VoidVoice.model.Finger> generateRightFingerList() {
       /* ru.mtl.VoidVoice.model.Finger tmp = new ru.mtl.VoidVoice.model.Finger();

        tmp.setFingerType(FingerType.Thumb);
        tmp.setFingerDirectionVector(new Vector3d(rightThumbDir));
        tmp.setCurvature(rightPalmNormal, rightThumbDir);
        tmp.setFingerTipPosition(new Point3d(rightThumbPos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Index);
        tmp.setFingerDirectionVector(new Vector3d(rightIndexDir));
        tmp.setCurvature(rightPalmNormal, rightIndexDir);
        tmp.setFingerTipPosition(new Point3d(rightIndexPos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Middle);
        tmp.setFingerDirectionVector(new Vector3d(rightMiddleDir));
        tmp.setCurvature(rightPalmNormal, rightMiddleDir);
        tmp.setFingerTipPosition(new Point3d(rightMiddlePos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Ring);
        tmp.setFingerDirectionVector(new Vector3d(rightRingDir));
        tmp.setCurvature(rightPalmNormal, rightRingDir);
        tmp.setFingerTipPosition(new Point3d(rightMiddlePos));
        res.add(tmp);

        tmp = new ru.mtl.VoidVoice.model.Finger();
        tmp.setFingerType(FingerType.Pinky);
        tmp.setFingerDirectionVector(new Vector3d(rightPinkyDir));
        tmp.setCurvature(rightPalmNormal, rightPinkyDir);
        tmp.setFingerTipPosition(new Point3d(rightPinkyPos));
        res.add(tmp);*/
        List<ru.mtl.VoidVoice.model.Finger> res = new ArrayList<>();
        addToFingerList(res, FingerType.Thumb, rightThumbDir, rightPalmNormal, rightThumbPos);
        addToFingerList(res, FingerType.Index, rightIndexDir, rightPalmNormal, rightIndexPos);
        addToFingerList(res, FingerType.Middle, rightMiddleDir, rightPalmNormal, rightMiddlePos);
        addToFingerList(res, FingerType.Ring, rightRingDir, rightPalmNormal, rightRingPos);
        addToFingerList(res, FingerType.Pinky, rightPinkyDir, rightPalmNormal, rightPinkyPos);

        return res;
    }

    private void averageHands() {
        averageLeftHandsValues();
        averageRightHandsValues();
    }

    private void averageLeftHandsValues() {
        final float n = leftHandsNumber * 1.0f;
        leftPalmNormal = coordinateDivider(leftPalmNormal, n);
        leftHandDirection = coordinateDivider(leftHandDirection, n);
        leftConfidence /= n;
        leftHandVelocity = coordinateDivider(leftHandVelocity, n);

        leftPinkyDir = coordinateDivider(leftPinkyDir, n);
        leftPinkyPos = coordinateDivider(leftPinkyPos, n);

        leftRingDir = coordinateDivider(leftRingDir, n);
        leftRingPos = coordinateDivider(leftRingPos, n);

        leftMiddleDir = coordinateDivider(leftMiddleDir, n);
        leftMiddlePos = coordinateDivider(leftMiddlePos, n);

        leftIndexDir = coordinateDivider(leftIndexDir, n);
        leftIndexPos = coordinateDivider(leftIndexPos, n);

        leftThumbDir = coordinateDivider(leftThumbDir, n);
        leftThumbPos = coordinateDivider(leftThumbPos, n);
    }

    private void averageRightHandsValues() {
        final float n = rightHandsNumber * 1.0f;
        rightPalmNormal = coordinateDivider(rightPalmNormal, n);
        rightHandDirection = coordinateDivider(rightHandDirection, n);
        rightConfidence /= n;
        rightHandVelocity = coordinateDivider(rightHandVelocity, n);

        rightPinkyDir = coordinateDivider(rightPinkyDir, n);
        rightPinkyPos = coordinateDivider(rightPinkyPos, n);

        rightRingDir = coordinateDivider(rightRingDir, n);
        rightRingPos = coordinateDivider(rightRingPos, n);

        rightMiddleDir = coordinateDivider(rightMiddleDir, n);
        rightMiddlePos = coordinateDivider(rightMiddlePos, n);

        rightIndexDir = coordinateDivider(rightIndexDir, n);
        rightIndexPos = coordinateDivider(rightIndexPos, n);

        rightThumbDir = coordinateDivider(rightThumbDir, n);
        rightThumbPos = coordinateDivider(rightThumbPos, n);
    }

    private Vector coordinateSummer(Vector first, Vector second) {
        //return new Vector(first.getX() + second.getX(), first.getY() + second.getY(),
        //      first.getZ() + second.getZ());
        Vector tmp = new Vector();
        tmp.setX(first.getX() + second.getX());
        tmp.setY(first.getY() + second.getY());
        tmp.setZ(first.getZ() + second.getZ());
        return tmp;
    }

    private Vector coordinateDivider(Vector v, float number) {
        Vector tmp = new Vector();
        tmp.setX(v.getX() / number);
        tmp.setY(v.getY() / number);
        tmp.setZ(v.getZ() / number);
        return tmp;
    }

    private void plusLeftFingers(Hand hand) {
        for (Finger finger : hand.fingers()) {
            switch (finger.type().name()) {
                case "TYPE_PINKY":
                    leftPinkyDir = coordinateSummer(leftPinkyDir, finger.direction());
                    leftPinkyPos = coordinateSummer(leftPinkyPos, finger.tipPosition());
                    break;
                case "TYPE_RING":
                    leftRingDir = coordinateSummer(leftRingDir, finger.direction());
                    leftRingPos = coordinateSummer(leftRingPos, finger.tipPosition());
                    break;
                case "TYPE_MIDDLE":
                    leftMiddleDir = coordinateSummer(leftMiddleDir, finger.direction());
                    leftMiddlePos = coordinateSummer(leftMiddlePos, finger.tipPosition());
                    break;
                case "TYPE_INDEX":
                    leftIndexDir = coordinateSummer(leftIndexDir, finger.direction());
                    leftMiddlePos = coordinateSummer(leftMiddlePos, finger.tipPosition());
                    break;
                case "TYPE_THUMB":
                    leftThumbDir = coordinateSummer(leftThumbDir, finger.direction());
                    leftThumbPos = coordinateSummer(leftThumbPos, finger.tipPosition());
                    break;
            }
        }
    }

    private void plusRightFingers(Hand hand) {
        for (Finger finger : hand.fingers()) {
            switch (finger.type().name()) {
                case "TYPE_PINKY":
                    rightPinkyDir = coordinateSummer(rightPinkyDir, finger.direction());
                    rightPinkyPos = coordinateSummer(rightPinkyPos, finger.tipPosition());
                    break;
                case "TYPE_RING":
                    rightRingDir = coordinateSummer(rightRingDir, finger.direction());
                    rightRingPos = coordinateSummer(rightRingPos, finger.tipPosition());
                    break;
                case "TYPE_MIDDLE":
                    rightMiddleDir = coordinateSummer(rightMiddleDir, finger.direction());
                    rightMiddlePos = coordinateSummer(rightMiddlePos, finger.tipPosition());
                    break;
                case "TYPE_INDEX":
                    rightIndexDir = coordinateSummer(rightIndexDir, finger.direction());
                    rightIndexPos = coordinateSummer(rightMiddlePos, finger.tipPosition());
                    break;
                case "TYPE_THUMB":
                    rightThumbDir = coordinateSummer(rightThumbDir, finger.direction());
                    rightThumbPos = coordinateSummer(rightThumbPos, finger.tipPosition());
                    break;
            }
        }
    }


    public static int getAverageFrameNumber() {
        return AVERAGE_FRAME_NUMBER;
    }

    private void handsNuller() {
        if (leftHandsNumber == 0) {
            resMotionVector.setLeftHand(null);
            resMotionVector.setLeftHandMotion(null);
            resMotionVector.setLeftFingersList(null);
        }
        if (rightHandsNumber == 0) {
            resMotionVector.setRightHand(null);
            resMotionVector.setRightHandMotion(null);
            resMotionVector.setRightFingersList(null);
        }
    }
}
