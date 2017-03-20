package com.hiklife.rfidapi;

import java.io.PrintStream;

public class Crack
{
  static
  {
    try
    {
      System.loadLibrary("TripleDES");
    }
    catch (UnsatisfiedLinkError ule)
    {
      System.err.println("WARNING: Could not load library libTripleDES.so!");
    }
  }
  
  public static native int Decrypt(byte[] encryptOut,byte[] encryptIn, long datalen);
}
