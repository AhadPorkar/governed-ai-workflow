package io.github.ahadporkar.ordertocash.governance;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.function.Function;
/** Single-writer demo storage. Atomic replacement plus fsync; use PostgreSQL for multiple replicas. */
public final class FileStateStore implements StateStore {
    private final Path directory,file;
    private final FileChannel lockChannel;
    private final FileLock lock;
    private Map<String,Object> state;
    private boolean closed;
    public FileStateStore(Path directory) {
        this.directory=directory.toAbsolutePath().normalize();this.file=this.directory.resolve("governance-state.json");
        FileChannel channel=null;FileLock held=null;
        try {
            Files.createDirectories(this.directory);
            try { Files.setPosixFilePermissions(this.directory,PosixFilePermissions.fromString("rwx------")); } catch(UnsupportedOperationException ignored) {}
            channel=FileChannel.open(this.directory.resolve("writer.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
            held=channel.tryLock();if(held==null)throw new IllegalStateException("State directory already has a writer");
            lockChannel=channel;lock=held;
            if(Files.exists(file)) {
                if(Files.size(file)>64L*1024*1024)throw new IOException("State size limit");
                state=Json.object(Json.parseStored(Files.readString(file)));StateStore.validate(state);
            } else { state=StateStore.empty();persist(state); }
        } catch(IOException|RuntimeException e) {
            try { if(held!=null)held.release();if(channel!=null)channel.close(); } catch(IOException ignored) {}
            throw new IllegalStateException("Cannot open state store; do not delete existing data to bypass an integrity failure",e);
        }
    }
    @Override public synchronized Map<String,Object> transaction(Function<Map<String,Object>,Map<String,Object>> work) {
        ensureOpen();var draft=Data.copy(state);var result=Data.copy(work.apply(draft));StateStore.validate(draft);
        persist(draft);state=Data.copy(draft);return result;
    }
    @Override public synchronized Map<String,Object> snapshot() { ensureOpen();return Data.copy(state); }
    private void ensureOpen() { if(closed)throw new IllegalStateException("Store closed"); }
    private void persist(Map<String,Object> value) {
        Path temp=directory.resolve("governance-state.tmp");
        byte[] bytes=Json.stringify(value).getBytes(StandardCharsets.UTF_8);
        if(bytes.length>64*1024*1024)throw new IllegalStateException("Demo state exceeds 64 MiB; migrate to PostgreSQL");
        try {
            try(var c=FileChannel.open(temp,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)) {
                ByteBuffer b=ByteBuffer.wrap(bytes);while(b.hasRemaining())c.write(b);c.force(true);
            }
            try { Files.setPosixFilePermissions(temp,PosixFilePermissions.fromString("rw-------")); } catch(UnsupportedOperationException ignored) {}
            Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
            // Directory fsync is available on Unix. Windows does not expose directory channels.
            try(var d=FileChannel.open(directory,StandardOpenOption.READ)) { d.force(true); }
            catch(AccessDeniedException e) { if(!System.getProperty("os.name").toLowerCase().contains("win"))throw e; }
        } catch(IOException e) { closed=true;throw new IllegalStateException("Durable commit failed; restart and inspect the store before retrying",e); }
    }
    @Override public synchronized void close() {
        closed=true;
        try { if(lock.isValid())lock.release();lockChannel.close(); } catch(IOException e) { throw new IllegalStateException(e); }
    }
}
