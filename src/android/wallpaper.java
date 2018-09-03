package fc.fcstudio;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import org.apache.cordova.PluginResult;
import java.io.IOException;
import java.io.File;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

public class wallpaper extends CordovaPlugin
{
	public Context context = null;
	private static final boolean IS_AT_LEAST_LOLLIPOP = Build.VERSION.SDK_INT >= 21;
	
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
	{
		context = IS_AT_LEAST_LOLLIPOP ? cordova.getActivity().getWindow().getContext() : cordova.getActivity().getApplicationContext();
		String imgSrc = "";
		Boolean base64 = false;
		
		if (action.equals("start"))
		{
			imgSrc = args.getString(0);
			base64 = args.getBoolean(1);
			this.echo(imgSrc, base64, context);
			PluginResult pr = new PluginResult(PluginResult.Status.OK);
			pr.setKeepCallback(true);
			callbackContext.sendPluginResult(pr);
			return true;
		}
		callbackContext.error("Set wallpaper is not a supported.");
        	return false;
	}

	public void echo(String image, Boolean base64, Context context)
	{
		try
		{
			AssetManager assetManager = context.getAssets();
			if(base64) //Base64 encoded
			{
				byte[] decoded = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
				Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
				WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
				myWallpaperManager.setBitmap(bitmap);
			}
			else //normal path
			{
				image = image.substring(7);
				File tempFile = new File(image);
				Uri contentURI = getImageContentUri(cordova.getActivity().getApplicationContext(), tempFile.getAbsolutePath());
				Intent setAsIntent = new Intent(Intent.ACTION_ATTACH_DATA);
				setAsIntent.setDataAndType(contentURI, "image/*");
				setAsIntent.putExtra("mimeType", "image/*");
				cordova.setActivityResultCallback (this);
				cordova.getActivity().startActivityForResult(Intent.createChooser(setAsIntent, "Set as"), 1);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Uri getImageContentUri(Context context, String absPath) {
		Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
				new String[] { absPath }, null);

		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));

		} else if (!absPath.isEmpty()) {
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.DATA, absPath);
			return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			return null;
		}
	}

}
