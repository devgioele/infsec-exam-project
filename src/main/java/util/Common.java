package util;

public class Common {

	public static byte[] concatByteArrays(byte[]... arrays) {
		int totalLength = 0;
		for(byte[] arr : arrays) {
			totalLength += arr.length;
		}
		byte[] total = new byte[totalLength];
		int destPos = 0;
		for(byte[] arr : arrays) {
			System.arraycopy(arr, 0, total, destPos, arr.length);
			destPos += arr.length;
		}
		return total;
	}

	public static boolean anyNull(Object... objects) {
		for(Object obj : objects) {
			if(obj == null)
				return true;
		}
		return false;
	}

}
