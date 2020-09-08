import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import game.Game;
import handhistory.HandHistory;
import handhistory.TrainingData;
import handhistory.util;

public class Main {



	// TODO later for genetic algo, after playing tournament(s) group player by
	// playing style tag/lag, rock/calling station and get the best from every group
	// -> ai is trained to play against all playing styles
	// maybe some inter spezies kreuzung but mostly intra
	// also more player in next gen of dominating species

	// TODO 3!!!!debug and test all by training data to string
	// in two steps(first test normal data, second vergleich normal data mit vec
	// data)

	// TODO 2 comment shit

	// TODO vor- und nachbedingung per assert


	private static boolean withPrinting = true;

	public static void main(String[] args) throws IOException {

		Game g = new Game("OG1");

		TrainingData.closeWriters();
		System.out.println("DONE MF");
	}

	@SuppressWarnings("unused")
	private static void preProcess() throws IOException {
		clearTrainingData();

		int count;
		try (Stream<Path> files = Files.list(Paths.get(util.DATA_PATH))) {
			count = (int) files.count();
		}

		for (int i = 0; i < count; i++) {
			HandHistory handHistory = new HandHistory(util.DATA_PATH
					.concat(util.DATA_PRENAME.concat(String.valueOf(i + 1)).concat(".txt")));
			handHistory.readAllHands(withPrinting);
		}
	}

	private static void clearTrainingData() {
		String[] pathNames = new String[] { "input/", "output/" };

		for (String pName : pathNames) {
			File dir = new File(util.TRAINING_DATA_PATH + pName);
			for (File file : dir.listFiles())
				if (!file.isDirectory())
					file.delete();
		}
	}
}
