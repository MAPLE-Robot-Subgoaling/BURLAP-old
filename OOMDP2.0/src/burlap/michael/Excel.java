package burlap.michael;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Excel {
	
	private WritableWorkbook workbook;
	private WritableSheet sheet;
	
	private int sumNumSteps;
	
	/**
	 *  Creates a new instance of an Excel object that is used to write to an
	 * Excel file and create a file with the results. The file must end with
	 * the extension '.xls'
	 * from running the simulation.
	 * @param outputPath - The path to output the data to. (i.e. "data.xls")
	 */
	public Excel(String outputPath){
		
		// This check only assumes that the was no extension to the file.
		if(!outputPath.endsWith(".xls")){
			outputPath = outputPath + ".xls";
		}
		
		
		sumNumSteps = 0;
		
		try {
			workbook = Workbook.createWorkbook(new File(outputPath));
			sheet = workbook.createSheet("First Sheet", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Records the data from the episode.
	 * @param episodeNumber - the current episode number (this is usually the
	 *                        variable 'i' because the episodes are simulated
	 *                        in a for loop with the variable 'i')
	 * @param numSteps - the number of steps the agent took to find the goal.
	 *                   This value comes from the EpisodeAnalysis object.
	 */
	public void recordData(int episodeNumber, int numSteps){
		sumNumSteps += numSteps;
		
		Number episode = new Number(episodeNumber, 0, episodeNumber);
		Number cumulativeSteps = new Number(episodeNumber, 1, sumNumSteps);
		
		
		try {
			sheet.addCell(episode);
			sheet.addCell(cumulativeSteps);
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			workbook.write();
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
