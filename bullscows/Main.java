package bullscows;

import java.util.Scanner;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.println("Input the length of the secret code:");
		int digitsCount = 0;
		try {
			digitsCount = sc.nextInt();
		} catch (Exception e) {
			System.out.println("Error: this is not a valid number");
			return;
		}
		if (digitsCount > 36 || digitsCount < 1) {
			System.out.printf("Error: can't generate a secret number with a length of %d.\n", digitsCount);
			return;
		}

		System.out.println("Input the number of possible symbols in the code:");
		int possibleSymbolsCount = 0;
		try {
			possibleSymbolsCount = sc.nextInt();
		} catch (Exception e) {
			System.out.println("Error: this is not a valid number");
			return;
		}
		if (possibleSymbolsCount < digitsCount) {
			System.out.printf("Error: it's not possible to generate a code with a length of %d with %d unique symbols.\n", digitsCount, possibleSymbolsCount);
			return;
		} else if (possibleSymbolsCount > 36) {
			System.out.println("Error: maximum number of possible symbols in the code is 36 (0-9, a-z).");
			return;
		}

		StringBuilder secretCode = GetRandomSecretCode (digitsCount, possibleSymbolsCount);
		System.out.println("Okay, let's start a game!");

		int turnsCount = 1;
		BullsCowsGrade bcGrade = new BullsCowsGrade();

		while (bcGrade.bulls != digitsCount) {
			System.out.printf("Turn  %d:\n", turnsCount);
			bcGrade = Grader(sc.next(), secretCode.toString());

			if (bcGrade.bulls == digitsCount) {
				System.out.printf("Grade: %d bull(s).\n", bcGrade.bulls);
				System.out.println("Congratulations! You guessed the secret code.");
			} else if (bcGrade.bulls == 0 && bcGrade.cows == 0) {
				System.out.println("Grade: None.");
			} else if (bcGrade.bulls == 0) {
				System.out.printf("Grade: %d cow(s).\n", bcGrade.cows);
			} else if (bcGrade.cows == 0) {
				System.out.printf("Grade: %d bull(s).\n", bcGrade.bulls);
			} else {
				System.out.printf("Grade: %d bull(s) and %d cow(s).\n", bcGrade.bulls, bcGrade.cows);
			}
			turnsCount++;
		}

		sc.close();
	}

	private static StringBuilder GetRandomSecretCode (int digitsCount, int possibleSymbolsCount) {
		StringBuilder secretCode = new StringBuilder();
		String possibleSymbols = "0123456789abcdefghijklmnopqrstuvwxyz";

		Random rand = new Random();

		while (secretCode.length() < digitsCount) {
			char randSymb = possibleSymbols.charAt(rand.nextInt(possibleSymbolsCount));
			if (secretCode.indexOf(String.valueOf(randSymb)) == -1) {
				secretCode.append(randSymb);
			}

		}

		String rangeStr;
		if (possibleSymbolsCount <= 10) {
			rangeStr = "0-" + String.valueOf(possibleSymbolsCount - 1);
		} else if (possibleSymbolsCount == 11) {
			rangeStr = "0-9, a";
		} else {
			rangeStr = "0-9, a-" + possibleSymbols.charAt(possibleSymbolsCount-1);
		}

		System.out.printf("The secret code is prepared: %s (%s)\n", "*".repeat(digitsCount), rangeStr);
		return secretCode;
	}

	public static BullsCowsGrade Grader(String guessStr, String secretCode) {
		int cows = 0;
		int bulls = 0;
		for (int i = 0; i < guessStr.length(); i++) {
			for (int j = 0; j < secretCode.length(); j++) {
				if (guessStr.charAt(i) == secretCode.charAt(j) && i != j) {
					cows++;
				} else if (guessStr.charAt(i) == secretCode.charAt(j) && i == j) {
					bulls++;
				}
			}
		}
		return new BullsCowsGrade(bulls, cows);
	}
}

class BullsCowsGrade {
	public int bulls;
	public int cows;

	public BullsCowsGrade () {
		this.bulls = 0;
		this.cows = 0;
	}

	public BullsCowsGrade (int bulls, int cows) {
		this.bulls = bulls;
		this.cows = cows;
	}
}