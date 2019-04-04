package model;

import java.io.File;

public class Image {
	private File imageFile;
	private File annotationFile;
	private String name;
	private int width;
	private int height;
	private double[] bndBox;
	private double[] mappedBndBox;

	public Image(File imageFile, File annotationFile, String name, int width, int height, double[] bndBox, double[] mappedBndBox) {
		this.imageFile = imageFile;
		this.annotationFile = annotationFile;
		this.name = name;
		this.width = width;
		this.height = height;
		this.bndBox = bndBox;
		this.mappedBndBox = mappedBndBox;
	}

	public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	public File getAnnotationFile() {
		return annotationFile;
	}

	public void setAnnotationFile(File annotationFile) {
		this.annotationFile = annotationFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public double[] getBndBox() {
		return bndBox;
	}

	public void setBndBox(double[] bndBox) {
		this.bndBox = bndBox;
	}

	public double[] getMappedBndBox() {
		return mappedBndBox;
	}

	public void setMappedBndBox(double[] mappedBndBox) {
		this.mappedBndBox = mappedBndBox;
	}
}
