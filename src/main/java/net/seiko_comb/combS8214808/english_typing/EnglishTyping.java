package net.seiko_comb.combS8214808.english_typing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import processing.core.PApplet;

public class EnglishTyping extends PApplet {
	public static void main(String... args) {
		PApplet.main(EnglishTyping.class.getName());
	}

	public void settings() {
		size(640, 360);
	}

	private List<EnglishTypingQuestion> questionsList = new ArrayList<>();

	public void setup() {
		Path path = Paths.get("data", "list.txt");
		if (Files.exists(path)) {
			try {
				Files.lines(path).forEach(line -> {
					try {
						String[] split = line.split("\t");
						String[] values = split[1].split("-");
						loadQuestion(split[0], Integer.parseInt(values[0]) - 1,
								Integer.parseInt(values[1]));
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			loadQuestion("1461-1490.txt", 0, 30);
		}
		nextQuestion();
		textFont(createFont("HGP創英角ｺﾞｼｯｸUB", 80));
	}

	private EnglishTypingQuestion nowQuestion;
	private int questionCount = -1;
	private int typed = 0;
	private int miss = 0;
	private int missMillis = -10000;
	private int startMillis = -1;
	private int millisNeeded = -1;
	private int overallTyped = 0;
	private int overallMiss = 0;
	private String lastWord = "";
	private int lastFinish = -10000;
	private boolean displayingResult = false;

	private void reset() {
		typed = 0;
		miss = 0;
		overallTyped = 0;
		overallMiss = 0;
		millisNeeded = millis() - startMillis;
		startMillis = -1;
		displayingResult = true;
	}

	public void draw() {
		if (displayingResult) {
			background(128);
			fill(0);
			textSize(30);
			textAlign(CENTER);
			text(String.format("Total Time : %.3f sec", millisNeeded / 1000.0), width / 2, height / 3);
			text("PRESS SPACE TO START", width / 2, height * 2 / 3);
		} else {
			float f = constrain(map(millis(), missMillis, missMillis + 300, 0, 255), 128, 255);
			background(255, f, f);
			textAlign(LEFT);
			float x, y;
			y = 120;
			x = 60;
			fill(255, 0, 0);
			textSize(40);
			text(nowQuestion.getJapaneseRed(), x, y);
			x += textWidth(nowQuestion.getJapaneseRed());
			fill(0);
			textSize(30);
			text(nowQuestion.getJapenseBlack(), x, y);
			y = 240;
			x = 60;
			textSize(30);
			if (millis() <= lastFinish + 1000) {
				float z = map(millis(), lastFinish, lastFinish + 1000, 0, 255);
				fill(z);
				text(lastWord, x, y - z / 3);
			}
			if (miss >= 10) {
				fill(192);
				text(nowQuestion.getEnglish().substring(0,
						constrain((miss - 20) / 2 + typed, 1, nowQuestion.getEnglish().length())), x, y);
			} else {
				fill(192);
				text(nowQuestion.getEnglish().substring(0, 1), x, y);
			}
			fill(0);
			text(nowQuestion.getEnglish().substring(0, typed), x, y);
			y = 300;
			x = 80;
			textSize(15);
			if (startMillis != -1) {
				text(String.format("%.1ftypes/min typed=%d miss=%d", overallTyped * 60000.0 / (millis() - startMillis),
						overallTyped, overallMiss), x, y);
			}
			fill(0, 0, 255);
			noStroke();
			rect(0, height, map(questionCount, 0, questionsList.size(), 0, width), -15);
		}
	}

	public void keyTyped() {
		if (displayingResult) {
			if (key == ' ') {
				displayingResult = false;
			}
		} else {
			if (startMillis == -1)
				startMillis = millis();
			if (key == '>') {
				nextQuestion();
			} else if (key == 'S') {
				playSound(nowQuestion.getEnglish() + ".wav");
			} else if ('a' <= key && key <= 'z') {
				if (key == nowQuestion.getEnglish().charAt(typed)) {
					typed++;
					overallTyped++;
					if (typed == nowQuestion.getEnglish().length()) {
						lastWord = nowQuestion.getEnglish();
						lastFinish = millis();
						nextQuestion();
					}
				} else {
					miss++;
					if (miss == 5) {
						playSound(nowQuestion.getEnglish() + ".wav");
					}
					overallMiss++;
					missMillis = millis();
				}
			} else if (key == ' ') {
				overallTyped -= typed;
				overallMiss -= miss;
				typed = 0;
				miss = 0;
			}
		}
	}

	private void playSound(String file) {
		new Thread(() -> {
			// Read the sound file using AudioInputStream.
			try {
				AudioInputStream stream;
				stream = AudioSystem.getAudioInputStream(new File("data/snd/" + file));
				byte[] buf = new byte[stream.available()];
				stream.read(buf, 0, buf.length);

				// Get an AudioFormat object from the stream.
				AudioFormat format = stream.getFormat();
				long nBytesRead = format.getFrameSize() * stream.getFrameLength();

				// Construct a DataLine.Info object from the format.
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
				SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

				// Open and start the line.
				line.open(format);
				line.start();

				// Write the data out to the line.
				line.write(buf, 0, (int) nBytesRead);

				// Drain and close the line.
				line.drain();
				line.close();
			} catch (UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}

	private void nextQuestion() {
		questionCount++;
		questionCount %= questionsList.size();
		if (questionCount == 0) {
			Collections.shuffle(questionsList);
			reset();
		}
		nowQuestion = questionsList.get(questionCount);
		typed = 0;
		miss = 0;
	}

	private void loadQuestion(String file, int begin, int end) {
		try {
			// Files.lines(Paths.get("data", file))
			// .forEach(System.out::println);
			Files.lines(Paths.get("data", file)).skip(begin).limit(end - begin)
					// .peek(System.out::println)
					.map(EnglishTypingQuestion::parse).filter(Optional::isPresent).map(Optional::get)
					.forEach(questionsList::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
