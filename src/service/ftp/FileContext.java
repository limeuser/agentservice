package service.ftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileContext {
    private File file;
    private FileOutputStream out;
    private int dataLength = 0;
    private byte[] buffer = new byte[FtpServer.BufferSize];
    
    private FileContext() {}
    public final static FileContext newFileContext(String fname, StringBuilder error) {
        FileContext ctx = new FileContext();
        ctx.file = new File(fname);
        if (ctx.file.exists()) {
            error.append("file exists:" + fname);
            return null;
        }
        
        try {
            ctx.out = new FileOutputStream(ctx.file);
        } catch (FileNotFoundException e) {
            error.append("file disappered:" + fname);
            return null;
        }
        
        return ctx;
    }
    
    public boolean flush() {
        try {
            out.write(buffer, 0, dataLength);
            this.dataLength = 0;
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public boolean done() {
        try {
            if (flush() == false) {
                this.out.close();
                return false;
            }
            this.out.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public int getRemainingSize() {
        return buffer.length - dataLength;
    }
    
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public FileOutputStream getOut() {
        return out;
    }
    public void setOut(FileOutputStream out) {
        this.out = out;
    }
    public int getDataLength() {
        return dataLength;
    }
    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }
    public byte[] getBuffer() {
        return buffer;
    }
    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
