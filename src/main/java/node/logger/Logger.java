package node.logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class writes log files about node calculations.
 */
public class Logger {

	ThreadLocal<DateFormat> formatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMdd_HHmmss.SSS");
		}
	};

	/**
	 * Writes a log file to a specified directory.
	 * 
	 * @param fileDir the directory of the file
	 * @param nodeId the id of the node to use in the file name
	 * @param request the request to be logged
	 * @param result the result that is logged
	 */
	public void log(String fileDir, int nodeId, String request, String result) {
		String fileName = formatter.get().format(new Date()) + "_node" + nodeId + ".log";

		try {

			File file = new File(fileDir);
			file.mkdirs();
			PrintWriter writer = new PrintWriter(fileDir + File.separator + fileName);
			writer.println(request);
			writer.println(result);
			writer.close();

		} catch (IOException e) {
			throw new RuntimeException("Error writing log", e);
		}
	}
}
