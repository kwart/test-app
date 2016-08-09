public class ConsumeHeap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getUnsafe().allocateMemory(4L*2000000000);
		System.out.println(new int[args.length>0?Integer.parseInt(args[0]):2000000000].length);
	}

	@SuppressWarnings("restriction")
    private static sun.misc.Unsafe getUnsafe() {
        try {
            java.lang.reflect.Field singleoneInstanceField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (sun.misc.Unsafe) singleoneInstanceField.get(null);
        } catch (Exception e) {
        	return null;
        }
    }
}
