package mjoys.agent.service.ftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import mjoys.util.Logger;

public class FileContext {
    private File file;
    private FileOutputStream out;
    private int bufferWriteIndex = 0;
    private int recvLength = 0;
    private int expectedRecvLength = 0;
    private byte[] buffer = new byte[FtpServer.BufferSize];
    private static final Logger logger = new Logger().addPrinter(System.out);
    
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
    
    public void read(int length) {
    	this.bufferWriteIndex += length;
    	this.recvLength += length;
    	logger.log("read data:%d", length);
    }
    
    public boolean flush() {
        try {
            out.write(buffer, 0, bufferWriteIndex);
            logger.log("write data:%s", bufferWriteIndex);
            this.bufferWriteIndex = 0;
            return true;
        } catch (IOException e) {
        	logger.log("write data exception", e);
            return false;
        }
    }
    
    public boolean done() {
        try {
            if (flush() == false) {
                this.out.close();
                return false;
            }
            if (expectedRecvLength <= 0) {
            	logger.log("don't knowen length when done");
            	return false;
            }
            	
            if (recvLength == expectedRecvLength) {
            	this.out.close();
            	logger.log("recv file success");
            	return true;
            }
            
            return false;
        } catch (IOException e) {
        	logger.log("close file output stream exception", e);
            return false;
        }
    }
    
    public int getRemainingSize() {
        return buffer.length - bufferWriteIndex;
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
        return bufferWriteIndex;
    }
    public void setDataLength(int dataLength) {
        this.bufferWriteIndex = dataLength;
    }
    public byte[] getBuffer() {
        return buffer;
    }
    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
    public int getRecvLength() {
    	return this.recvLength;
    }
    public void setExpectedRecvLength(int length) {
    	this.expectedRecvLength = length;
    }
}
