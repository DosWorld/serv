package com.cmlteam.serv;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

class TarUtil {

  private TarUtil() {}

  static void compress(OutputStream outputStream, File folder, boolean compress)
      throws IOException {
    try (TarArchiveOutputStream out = getTarArchiveOutputStream(outputStream, compress)) {
      File[] files = folder.listFiles();
      if (files != null) {
        for (File file : files) {
          addToArchiveCompression(out, file, ".");
        }
      }
    }
  }

  private static TarArchiveOutputStream getTarArchiveOutputStream(
      OutputStream outputStream, boolean compress) throws IOException {
    if (compress) {
      outputStream = new GzipCompressorOutputStream(outputStream);
    }
    TarArchiveOutputStream taos = new TarArchiveOutputStream(outputStream);
    // TAR has an 8 gig file limit by default, this gets around that
    taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
    // TAR originally didn't support long file names, so enable the support for it
    taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    taos.setAddPaxHeadersForNonAsciiNames(true);
    return taos;
  }

  private static void addToArchiveCompression(TarArchiveOutputStream out, File file, String dir)
      throws IOException {
    String entry = dir + File.separator + file.getName();
    if (file.isFile()) {
      out.putArchiveEntry(new TarArchiveEntry(file, entry));
      try (FileInputStream in = new FileInputStream(file)) {
        IOUtils.copy(in, out);
      }
      out.closeArchiveEntry();
    } else if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          addToArchiveCompression(out, child, entry);
        }
      }
    } else {
      System.out.println(file.getName() + " is not supported");
    }
  }
}
