/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */
package ejdelrosario.framework.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class FileUtil {

	
	public enum FileStatus {

		WRITE_SUCCESSFUL("Successfully saved file"),
		READ_SUCCESSFUL("Sucessfully read file"),
		SD_UNMOUNTED("SD card not mounted"),
		WRITE_FAILED("Failed to save file"),
		READ_FAILED("Failed to read file"),
		UNKNOWN_ERROR("Unexpected Error Occurred"), 
		NO_FILES("No files");

		String status = "";

		FileStatus(String status) {
			this.status = status;
		}

		public String toString() {
			return status;
		}

	}

	public static String getSDState() {
		return Environment.getExternalStorageState();
	}

	
	public static FileStatus saveImage(String dirName, String fileName, byte[] data) throws IOException {


		String sdState = Environment.getExternalStorageState();

		if(sdState.equals( Environment.MEDIA_MOUNTED) ) {

			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

			File dir = new File( dirName );

			if(!dir.exists())
				dir.mkdirs();

			File file = new File( dir, fileName );

			if(file.exists())
				file.delete();

			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();

			return FileStatus.WRITE_SUCCESSFUL;

		}
		else
			return FileStatus.SD_UNMOUNTED;
	}
	

	public static FileStatus saveImage(String dirName, String fileName, Bitmap bitmap) throws IOException {


		String sdState = Environment.getExternalStorageState();

		if(sdState.equals( Environment.MEDIA_MOUNTED) ) {

			File dir = new File( dirName );

			if(!dir.exists())
				dir.mkdirs();

			File file = new File( dir, fileName );

			if(file.exists())
				file.delete();

			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();

			return FileStatus.WRITE_SUCCESSFUL;

		}
		else
			return FileStatus.SD_UNMOUNTED;
	}

	
	public static FileStatus saveFile(String dirName, String fileName, byte[] data) throws IOException {

		String sdState = Environment.getExternalStorageState();

		if(sdState.equals( Environment.MEDIA_MOUNTED) ) {

			File dir = new File( dirName );

			if(!dir.exists())
				dir.mkdirs();

			File file = new File( dir, fileName );

			if(file.exists())
				file.delete();

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();

			return FileStatus.WRITE_SUCCESSFUL;

		}
		else
			return FileStatus.SD_UNMOUNTED;
	}


	public static FileStatus zipFile(String zipPath, String zipFileName , String folderName ,String...files ) throws IOException {

		int BUFFER = 2048;

		File dir = new File( zipPath );

		if(!dir.exists())
			dir.mkdirs();

		File file = new File( dir, zipFileName );

		BufferedInputStream origin = null;
		FileOutputStream dest = new FileOutputStream( file );
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream( dest ));
		byte data[] = new byte[BUFFER];

		for (int i = 0; i < files.length; i++) {

			System.out.println("Compress: " + "Adding: " + files[i]);
			FileInputStream fi = new FileInputStream(files[i]);
			origin = new BufferedInputStream(fi, BUFFER);

			ZipEntry entry = new ZipEntry( ( folderName != null ? folderName + "/" : "" ) + files[i].substring(files[i].lastIndexOf("/") + 1) );
			out.putNextEntry(entry);
			int count;

			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}

			origin.close();

		}

		out.close();

		return FileStatus.WRITE_SUCCESSFUL;

	}

	public void unzip(String zipFile, String targetLocation) throws IOException {

		//create target location folder if not exist
		dirChecker(targetLocation);

		FileInputStream fin = new FileInputStream(zipFile);
		ZipInputStream zin = new ZipInputStream(fin);
		ZipEntry ze = null;

		while ((ze = zin.getNextEntry()) != null) {

			//create dir if required while unzipping
			if (ze.isDirectory()) {
				dirChecker(ze.getName());
			} 
			else {
				FileOutputStream fout = new FileOutputStream(targetLocation + ze.getName());

				for (int c = zin.read(); c != -1; c = zin.read()) {
					fout.write(c);
				}

				zin.closeEntry();
				fout.close();
			}

		}
		zin.close();

	}

	private void dirChecker(String dir) {
		// TODO Auto-generated method stub
		File file = new File( dir );
		if(file.exists()) file.mkdirs();
	}

	public static void deleteDir(File fileOrDirectory) {

		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteDir(child);

		fileOrDirectory.delete();

	}

	public static void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();

	}
	
	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {
	        return getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int column_index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(column_index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}
