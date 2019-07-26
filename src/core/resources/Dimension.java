package core.resources;

public class Dimension {

	public static int toPx(String pos, int maxPx) {
		if (pos.startsWith("+"))
			return toPx(pos.substring(1), maxPx);
		
		if (pos.contains("-"))
			return maxPx - toPx(pos.substring(1), maxPx);
		
		if (pos.contains("%")) {
			String[] spl = pos.split("%");
			String prc = spl[0];
			
			int out = (int) (Float.valueOf(prc) * maxPx / 100);
			if (spl.length > 1)
				out += toPx(pos.substring(prc.length() + 1), maxPx - out);
			return  out;
		}
		
		return Integer.valueOf(pos);
	}
}
