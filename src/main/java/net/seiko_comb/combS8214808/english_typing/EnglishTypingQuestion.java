package net.seiko_comb.combS8214808.english_typing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

public class EnglishTypingQuestion {
	private int red;
	private String japanese;
	private String english;

	public EnglishTypingQuestion(String japanese, String english) {
		this.japanese = japanese;
		this.english = english;
	}

	public static Optional<EnglishTypingQuestion> parse(String line) {
		String[] split = line.split("\t");
		if (split.length < 2) {
			System.out.format("ignoring \"%s\"", line);
			return Optional.empty();
		}
		EnglishTypingQuestion ret = new EnglishTypingQuestion(split[0],
				split[1]);
		int index = split[0].indexOf("|");
		if (index != -1) {
			ret.red = index;
			ret.japanese = ret.japanese.replaceFirst("\\|", "");
		}
		// System.out.println(ret);
		downloadFile(ret.english);
		return Optional.of(ret);
	}

	private static void downloadFile(String english) {
		try {
			File file = new File("data/snd/" + english + ".wav"); // 保存先
			if (file.exists()) return;
			URL url = new URL(
					"http://translate.weblio.jp/tts?ar=e4e3857478bda3a1&query="
							+ english); // ダウンロードする URL
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();

			FileOutputStream out = new FileOutputStream(file, false);
			byte[] buf = new byte[1024];
			int len = 0;

			System.out.println("Start downloading " + english);
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			out.flush();

			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "EnglishTypingQuestion [red=" + red + ", japanese=" + japanese
				+ ", english=" + english + "]";
	}

	public String getJapanese() {
		return japanese;
	}

	public String getJapaneseRed() {
		return japanese.substring(0, red);
	}

	public String getJapenseBlack() {
		return japanese.substring(red);
	}

	public String getEnglish() {
		return english;
	}
}
