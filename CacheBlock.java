
public class CacheBlock 
{
public boolean isDirty = false;
public int blockCount = 0;		// LRU replacement policy
public String[] dataBytes;
}
