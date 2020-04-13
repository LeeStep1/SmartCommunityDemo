package cn.bit.framework.data.common;

import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

public class IdBuilder {

    private long type;
    private long machineId;
    private long sequence;

    public IdBuilder(long type) {
        this(type, _genmachine);
    }

    public IdBuilder(long type, long machineId) {
        this(type, machineId, 0);
    }

    public IdBuilder(long type, long machineId, long sequence){
        if (type > typeMask || type < 0) {
            type &= typeMask;
        }
        // sanity check for machineId
        if (machineId > maxMachineId || machineId < 0) {
            machineId &= maxMachineId;
        }
        System.out.printf("builder starting. type bits %d, timestamp bits %d, machine id bits %d, sequence bits %d, machineId %d",
                typeBits, timestampBits, machineIdBits, sequenceBits, machineId);

        this.type = type;
        this.machineId = machineId;
        this.sequence = sequence;
    }

    private long twepoch = 1288834974657L;

    private long timestampBits = 38L;
    private long typeBits = 1L;
    private long machineIdBits = 12L;
    private long sequenceBits = 12L;

    private long maxTimestamp = -1L ^ (-1L << timestampBits);
    private long typeMask = -1L ^ (-1L << typeBits);
    private long maxMachineId = -1L ^ (-1L << machineIdBits);
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long timestampLeftShift = sequenceBits + machineIdBits + typeBits;
    private long typeLeftShift = sequenceBits + machineIdBits;
    private long machineIdShift = sequenceBits;

    private long lastTimestamp = -1L;

    private static final int _genmachine;

    public long getType() {
        return type;
    }

    public long getTimestamp(long id) {
        return (id >>> timestampLeftShift & maxTimestamp) + twepoch;
    }

    public long getSequence(long id) {
        return id & sequenceMask;
    }

    public long getMachineId(){
        return machineId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;
        return (((timestamp - twepoch) & maxTimestamp) << timestampLeftShift) |
                type << typeLeftShift |
                (machineId << machineIdShift) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen(){
        return System.currentTimeMillis();
    }

    static {
        try {
            int machinePiece;
            try {
                StringBuilder sb = new StringBuilder();
                Enumeration e = NetworkInterface.getNetworkInterfaces();

                while(e.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface)e.nextElement();
                    sb.append(ni.toString());
                }

                machinePiece = sb.toString().hashCode() << 16;
            } catch (Throwable var7) {
                System.err.println(var7.getMessage());
                machinePiece = (new Random()).nextInt() << 16;
            }

            System.err.println("machine piece post: " + Integer.toHexString(machinePiece));
            int processId = (new Random()).nextInt();

            try {
                processId = ManagementFactory.getRuntimeMXBean().getName().hashCode();
            } catch (Throwable var6) {
                ;
            }

            ClassLoader loader = IdBuilder.class.getClassLoader();
            int loaderId = loader != null ? System.identityHashCode(loader) : 0;
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toHexString(processId));
            sb.append(Integer.toHexString(loaderId));
            int processPiece = sb.toString().hashCode() & 0xffff;
            System.err.println("process piece: " + Integer.toHexString(processPiece));
            _genmachine = machinePiece | processPiece;
            System.err.println("machine : " + Integer.toHexString(_genmachine));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
