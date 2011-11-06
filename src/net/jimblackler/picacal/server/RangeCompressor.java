package net.jimblackler.picacal.server;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangeCompressor {
  @SuppressWarnings("serial")
  class CompressionException extends Exception {
    public CompressionException(String string) {
      super(string);
    }
  };

  private Map<BigInteger, Byte> fromMap;
  private BigInteger max;
  private Map<Byte, BigInteger> toMap;

  public RangeCompressor() {
    toMap = new HashMap<Byte, BigInteger>();
    fromMap = new HashMap<BigInteger, Byte>();
    byte[] valids = { 34, 44, 45, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69,
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92,
        93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
        112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122 };
    BigInteger value = BigInteger.ONE;
    for (byte n : valids) {
      toMap.put(n, value);
      fromMap.put(value, n);
      value = value.add(BigInteger.ONE);
    }
    max = value;
  }

  public byte[] compress(byte[] in) throws CompressionException {

    BigInteger num = BigInteger.ZERO;
    BigInteger multiplier = BigInteger.ONE;

    for (byte bt : in) {
      if (!toMap.containsKey(bt)) {
        throw new CompressionException("Unhandled byte: " + bt);
      }
      BigInteger converted = toMap.get(bt);
      num = num.add(multiplier.multiply(converted));
      multiplier = multiplier.multiply(max);
    }

    return num.toByteArray();
  }

  public byte[] decompress(byte[] in) throws CompressionException {
    BigInteger data = new BigInteger(in);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    while (!data.equals(BigInteger.ZERO)) {
      BigInteger[] divideAndRemainder = data.divideAndRemainder(max);
      data = divideAndRemainder[0];
      BigInteger value = divideAndRemainder[1];
      if (!fromMap.containsKey(value)) {
        throw new CompressionException("Decompression error");
      }
      bytes.write(fromMap.get(value));
    }
    return bytes.toByteArray();
  }

}
