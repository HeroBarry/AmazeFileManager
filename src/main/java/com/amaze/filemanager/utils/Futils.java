/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.utils;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amaze.filemanager.R;
import com.amaze.filemanager.activities.DbViewer;
import com.amaze.filemanager.activities.MainActivity;
import com.amaze.filemanager.adapters.HiddenAdapter;
import com.amaze.filemanager.fragments.AppsList;
import com.amaze.filemanager.fragments.Main;
import com.amaze.filemanager.services.asynctasks.GenerateMD5Task;
import com.amaze.filemanager.ui.Layoutelements;
import com.amaze.filemanager.ui.icons.Icons;
import com.amaze.filemanager.ui.icons.MimeTypes;
import com.amaze.filemanager.utils.share.ShareTask;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class Futils {

public  final int READ = 4;
    public  final int WRITE = 2;
    public  final int EXECUTE = 1;
    private Toast studioCount;

    public Futils() {
    }

    public static long folderSize(File directory) {
        long length = 0;
        try {
            for (File file:directory.listFiles()) {

                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }

    public static long folderSize(SmbFile directory) {
        long length = 0;
        try {
            for (SmbFile file:directory.listFiles()) {

                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }
    private  ColorStateList createEditTextColorStateList(int color) {
        int[][] states = new int[3][];
        int[] colors = new int[3];
        int i = 0;
        states[i] = new int[]{-android.R.attr.state_enabled};
        colors[i] = Color.parseColor("#f6f6f6");
        i++;
        states[i] = new int[]{-android.R.attr.state_pressed, -android.R.attr.state_focused};
        colors[i] = Color.parseColor("#666666");
        i++;
        states[i] = new int[]{};
        colors[i] =color ;
        return new ColorStateList(states, colors);
    }
    public  void setTint(CheckBox box, int color) {
        if(Build.VERSION.SDK_INT>=21)return;
        ColorStateList sl = new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        }, new int[]{
                Color.parseColor("#666666"),
                color
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            box.setButtonTintList(sl);
        } else {
            Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(box.getContext(), R.drawable.abc_btn_check_material));
            DrawableCompat.setTintList(drawable, sl);
            box.setButtonDrawable(drawable);
        }
    }
    public void setTint(EditText editText, int color) {
        if(Build.VERSION.SDK_INT>=21)return;
        ColorStateList editTextColorStateList = createEditTextColorStateList(color);
        if (editText instanceof AppCompatEditText) {
            ((AppCompatEditText) editText).setSupportBackgroundTintList(editTextColorStateList);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackgroundTintList(editTextColorStateList);
        }
    }

    public int checkFolder(final String f,Context context) {
        if(f.startsWith("smb://"))return 1;
        File folder=new File(f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && FileUtil.isOnExtSdCard(folder, context)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return 0;
            }

            // On Android 5, trigger storage access framework.
            if (FileUtil.isWritableNormalOrSaf(folder, context)) {
                return 1;

            }
        } else if (Build.VERSION.SDK_INT == 19 && FileUtil.isOnExtSdCard(folder, context)) {
            // Assume that Kitkat workaround works
            return 1;
        } else if (FileUtil.isWritable(new File(folder, "DummyFile"))) {
            return 1;
        } else {
            return 0;
        }
        return 0;
    }

    public void scanFile(String path, Context c) {
        System.out.println(path + " " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 19) {
            MediaScannerConnection.scanFile(c, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    System.out.println("SCAN COMPLETED: " + path);

                }
            });
        } else {
            Uri contentUri = Uri.fromFile(new File(path));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
            c.sendBroadcast(mediaScanIntent);
        }
    }

    public String getString(Context c, int a) {
        return c.getResources().getString(a);
    }


    public void shareFiles(ArrayList<File> a, Activity c,int theme,int fab_skin) {
        ArrayList<Uri> uris = new ArrayList<Uri>();
        boolean b = true;
        for (File f : a) {
            uris.add(Uri.fromFile(f));
        }
        System.out.println("uri done");
        String mime = MimeTypes.getMimeType(a.get(0));
        if (a.size() > 1)
            for (File f : a) {
                if (!mime.equals(MimeTypes.getMimeType(f))) {
                    b = false;
                }
            }

        if (!b || mime==(null))
            mime = "*/*";
        try {

            new ShareTask(c,uris,theme,fab_skin).execute(mime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    public String readableFileSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size
                / Math.pow(1024, digitGroups))
                + "" + units[digitGroups];
    }

    public String getFileExtension(String url) {
        try {
            int a = url.lastIndexOf(".");
            int b = url.length();
            return url.substring(a, b);
        } catch (Exception e) {
            return "";
        }

    }

    private boolean isSelfDefault(File f, Context c){
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(f), MimeTypes.getMimeType(f));
        String s="";
        ResolveInfo rii = c.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (rii !=  null && rii.activityInfo != null) s = rii.activityInfo.packageName;
        if (s.equals("com.amaze.filemanager") || rii==null) return true;
        else return false;
    }

    public void openunknown(File f, Context c, boolean forcechooser) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String type = MimeTypes.getMimeType(f);
        if(type!=null && type.trim().length()!=0 && !type.equals("*/*"))
        {intent.setDataAndType(Uri.fromFile(f), type);
        Intent startintent;
        if (forcechooser) startintent=Intent.createChooser(intent, c.getResources().getString(R.string.openwith));
        else startintent=intent;
        try {
            c.startActivity(startintent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        Toast.makeText(c,R.string.noappfound,Toast.LENGTH_SHORT).show();
        openWith(f,c);
        }}else{openWith(f, c);}

    }

public void openWith(final File f,final Context c) {
        MaterialDialog.Builder a=new MaterialDialog.Builder(c);
        a.title(getString(c, R.string.openas));
        String[] items=new String[]{getString(c,R.string.text),getString(c,R.string.image),getString(c,R.string.video),getString(c,R.string.audio),getString(c,R.string.database),getString(c,R.string.other)};
        a.items(items).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                switch (i) {
                    case 0:
                        intent.setDataAndType(Uri.fromFile(f), "text/*");
                        break;
                    case 1:
                        intent.setDataAndType(Uri.fromFile(f), "image/*");
                        break;
                    case 2:
                        intent.setDataAndType(Uri.fromFile(f), "video/*");
                        break;
                    case 3:
                        intent.setDataAndType(Uri.fromFile(f), "audio/*");
                        break;
                    case 4:
                        intent = new Intent(c, DbViewer.class);
                        intent.putExtra("path", f.getPath());
                        break;
                    case 5:
                        intent.setDataAndType(Uri.fromFile(f), "*/*");
                        break;
                }
                try {
                    c.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(c, R.string.noappfound, Toast.LENGTH_SHORT).show();
                    openWith(f, c);
                }
            }
        });
        try {
            a.build().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFiles(ArrayList<Layoutelements> a, final Main b, List<Integer> pos) {
        final MaterialDialog.Builder c = new MaterialDialog.Builder(b.getActivity());
        c.title(getString(b.getActivity(), R.string.confirm));
        final ContentResolver contentResolver=b.getActivity().getContentResolver();
        String names = "";
        final ArrayList<String> todelete = new ArrayList<>();
        for (int i = 0; i < pos.size(); i++) {
            String path = a.get(pos.get(i)).getDesc();
            todelete.add((path));
            if(path.startsWith("smb://"))
                try {
                    names = names + "\n" + (i + 1) + ". " + new SmbFile(path).getName();
                } catch (MalformedURLException e) {

                }
            else
            names = names + "\n" + (i + 1) + ". " + new File(path).getName();
        }
        c.content(getString(b.getActivity(), R.string.questiondelete) + names);

        if(b.theme1==1)
            c.theme(Theme.DARK);
        c.negativeText(getString(b.getActivity(), R.string.no));
        c.positiveText(getString(b.getActivity(), R.string.yes));
        c.positiveColor(Color.parseColor(b.fabSkin));
        c.negativeColor(Color.parseColor(b.fabSkin));
        c.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog materialDialog) {
            Toast.makeText(b.getActivity(), getString(b.getActivity(), R.string.deleting), Toast.LENGTH_SHORT).show();
            b.mainActivity.deleteFiles(todelete);
            }

            @Override
            public void onNegative(MaterialDialog materialDialog) {

                //materialDialog.cancel();
            }
        });
        c.build().show();
    }

    public boolean canGoBack(File f) {
        try {
            f.getParentFile().listFiles();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public String getdate(File f) {

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy | KK:mm a");
        return (sdf.format(f.lastModified())).toString();
    }
    public String getdate(long f) {

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy | KK:mm a");
        return (sdf.format(f)).toString();
    }

    public String getdate(long f,String form,String year) {

        SimpleDateFormat sdf = new SimpleDateFormat(form);
        String date=(sdf.format(f)).toString();
        if(date.substring(date.length()-2,date.length()).equals(year))
            date=date.substring(0,date.length()-6);
        return date;
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void showProps(final String f, final Main c,boolean root) {
        HFile hFile=new HFile(f);
        long last=0;
        try {
            last=hFile.lastModified();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
        String date = getString(c.getActivity(), R.string.date) + getdate(last);
        String items = getString(c.getActivity(), R.string.totalitems)+" calculating", size = getString(c.getActivity(), R.string.size)+" calculating", name, parent;
        name = getString(c.getActivity(), R.string.name) + hFile.getName();
        parent = getString(c.getActivity(), R.string.location) + hFile.getParent();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getActivity());
        String fabskin = PreferenceUtils.getFabColor(sp.getInt("fab_skin_color_position", 1));
        MaterialDialog.Builder a = new MaterialDialog.Builder(c.getActivity());
        a.title(getString(c.getActivity(), R.string.properties));
        if(c.theme1==1)
            a.theme(Theme.DARK);
        a.positiveText(R.string.copy_path);
        a.negativeText(getString(c.getActivity(), R.string.copy) + " md5");
        a.positiveColor(Color.parseColor(fabskin));
        a.negativeColor(Color.parseColor(fabskin));
        a.neutralText(R.string.cancel);
        a.neutralColor(Color.parseColor(fabskin));
        a.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog materialDialog) {

                c.mainActivity.copyToClipboard(c.getActivity(), f);
                Toast.makeText(c.getActivity(), c.getResources().getString(R.string.pathcopied), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNegative(MaterialDialog materialDialog) {
            }
        });
        MaterialDialog materialDialog=a.build();
        materialDialog.show();
        new GenerateMD5Task(materialDialog, hFile, name, parent, size, items, date,c.getActivity()).execute(f);
    }

    public void showProps(final File f, final Context c,int theme1) {
        String date = getString(c, R.string.date) + getdate(f);
        String items = getString(c, R.string.totalitems)+" calculating", size = getString(c, R.string.size)+" calculating", name, parent;
        name = getString(c, R.string.name) + f.getName();
        parent = getString(c, R.string.location) + f.getParent();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String fabskin = PreferenceUtils.getFabColor(sp.getInt("fab_skin_color_position", 1));
        MaterialDialog.Builder a = new MaterialDialog.Builder(c);
        a.title(getString(c, R.string.properties));
        if(theme1==1)
            a.theme(Theme.DARK);
        a.positiveText(R.string.copy_path);
        a.negativeText(getString(c, R.string.copy) + " md5");
        a.neutralText(R.string.cancel);
        a.positiveColor(Color.parseColor(fabskin));
        a.negativeColor(Color.parseColor(fabskin));
        a.neutralColor(Color.parseColor(fabskin));
        a.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog materialDialog) {

                copyToClipboard(c, f.getPath());
                Toast.makeText(c, c.getResources().getString(R.string.pathcopied), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNegative(MaterialDialog materialDialog) {
            }
        });

        MaterialDialog materialDialog=a.build();
        materialDialog.show();
        new GenerateMD5Task(materialDialog,new HFile( f.getPath()), name, parent, size, items, date,c).execute(f.getPath());
    }

    public boolean copyToClipboard(Context context, String text) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Path copied to clipboard", text);
            clipboard.setPrimaryClip(clip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Bundle getPaths(String path, Context c) {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> paths = new ArrayList<String>();
        Bundle b = new Bundle();
        while (path.contains("/")) {

            paths.add(path);
            names.add(path.substring(1 + path.lastIndexOf("/"), path.length()));
            path = path.substring(0, path.lastIndexOf("/"));
        }
        names.remove("");
        paths.remove("/");
        names.add("root");
        paths.add("/");
        // Toast.makeText(c,paths.get(0)+"\n"+paths.get(1)+"\n"+paths.get(2),Toast.LENGTH_LONG).show();
        b.putStringArrayList("names", names);
        b.putStringArrayList("paths", paths);
        return b;
    }

    public boolean deletedirectory(File f){
        boolean b=true;
        for(File file:f.listFiles()){
            boolean c;
            if(file.isDirectory()){c=deletedirectory(file);}
            else {c=file.delete();}
            if(!c)b=false;

        }if(b)b=f.delete();
        return b;
    }

    public boolean rename(File f, String name,boolean root) {
        String newname = f.getParent() + "/" + name;
        if(f.getParentFile().canWrite()){
            return f.renameTo(new File(newname));}
        else if(root) {
            RootTools.remount(f.getPath(),"rw");
            RootHelper.runAndWait("mv " + f.getPath() + " " + newname, true);
            RootTools.remount(f.getPath(),"ro");
        }return true;
    }

    public boolean canListFiles(File f) {
        try {
            if (f.canRead() && f.isDirectory())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void openFile(final File f, final MainActivity m) {
        boolean defaultHandler = isSelfDefault(f, m);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(m);
        if (defaultHandler && f.getName().toLowerCase().endsWith(".zip") || f.getName().toLowerCase().endsWith(".jar") || f.getName().toLowerCase().endsWith(".rar")|| f.getName().toLowerCase().endsWith(".tar")|| f.getName().toLowerCase().endsWith(".tar.gz")) {
            showArchiveDialog(f, m);
        } else if(f.getName().toLowerCase().endsWith(".apk")) {
            showPackageDialog(f, m);
        } else if (defaultHandler && f.getName().toLowerCase().endsWith(".db")) {
            Intent intent = new Intent(m, DbViewer.class);
            intent.putExtra("path", f.getPath());
            m.startActivity(intent);
        }  else if (Icons.isAudio(f.getPath())) {
            final int studio_count = sharedPreferences.getInt("studio", 0);
            Uri uri = Uri.fromFile(f);
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "audio/*");

            if (studio_count!=0) {
                new CountDownTimer(studio_count, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int sec = (int)millisUntilFinished/1000;
                        if (studioCount!=null)
                            studioCount.cancel();
                        studioCount = Toast.makeText(m, sec + "", Toast.LENGTH_LONG);
                        studioCount.show();
                    }

                    @Override
                    public void onFinish() {
                        if (studioCount!=null)
                            studioCount.cancel();
                        studioCount = Toast.makeText(m, "Opening..", Toast.LENGTH_LONG);
                        studioCount.show();
                        m.startActivity(intent);
                    }
                }.start();
            } else
                m.startActivity(intent);
        } else {
            try {
                openunknown(f, m, false);
            } catch (Exception e) {
                Toast.makeText(m, getString(m, R.string.noappfound),Toast.LENGTH_LONG).show();
                openWith(f, m);
            }
        }
    }

public void showPackageDialog(final File f,final MainActivity m){
    MaterialDialog.Builder mat=new MaterialDialog.Builder(m);
    mat.title(R.string.packageinstaller).content(R.string.pitext)
            .positiveText(R.string.install)
            .negativeText(R.string.view)
            .neutralText(R.string.cancel)
            .positiveColor(Color.parseColor(m.fabskin))
            .negativeColor(Color.parseColor(m.fabskin))
            .neutralColor(Color.parseColor(m.fabskin))
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog materialDialog) {
                    openunknown(f, m, false);
                }

                @Override
                public void onNegative(MaterialDialog materialDialog) {
                    m.openZip(f.getPath());
                }
            });
    if(m.theme1==1)mat.theme(Theme.DARK);
    mat.build().show();

}

    public void showArchiveDialog(final File f, final MainActivity m) {
        MaterialDialog.Builder mat = new MaterialDialog.Builder(m);
        mat.title(R.string.archive)
                .content(R.string.archtext)
                .positiveText(R.string.extract)
                .negativeText(R.string.view)
                .neutralText(R.string.cancel)
                .positiveColor(Color.parseColor(m.fabskin))
                .negativeColor(Color.parseColor(m.fabskin))
                .neutralColor(Color.parseColor(m.fabskin))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        m.extractFile(f);
                    }

                    @Override
                    public void onNegative(MaterialDialog materialDialog) {
                        //m.addZipViewTab(f.getPath());
                        if (f.getName().toLowerCase().endsWith(".rar"))
                            m.openRar(f.getPath());
                        else
                            m.openZip(f.getPath());
                    }
                });
        if (m.theme1 == 1) mat.theme(Theme.DARK);
        MaterialDialog b = mat.build();

        if (!f.getName().toLowerCase().endsWith(".rar") && !f.getName().toLowerCase().endsWith(".jar") && !f.getName().toLowerCase().endsWith(".apk") && !f.getName().toLowerCase().endsWith(".zip"))
            b.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
        b.show();

    }

    public Layoutelements newElement(Drawable i, String d,String permissions,String symlink,String size,long longSize,boolean directorybool,boolean b,String date) {
        Layoutelements item = new Layoutelements(i, new File(d).getName(), d,permissions,symlink,size,longSize,b,date,directorybool);
        return item;
    }

    public ArrayList<File> toFileArray(ArrayList<String> a) {
        ArrayList<File> b = new ArrayList<File>();
        for (int i = 0; i < a.size(); i++) {
            b.add(new File(a.get(i)));
        }
        return b;
    }

    public ArrayList<String> toStringArray(ArrayList<File> a) {
        ArrayList<String> b = new ArrayList<String>();
        for (int i = 0; i < a.size(); i++) {
            b.add(a.get(i).getPath());
        }
        return b;
    }

    public void showNameDialog(final MainActivity m, final ArrayList<String> b, final String current) {
        MaterialDialog.Builder a = new MaterialDialog.Builder(m);
        a.input(getString(m, R.string.enterzipname), new File(current).getName() + ".zip", false, new
                MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

                    }
                });
        a.widgetColor(Color.parseColor(m.fabskin));
        if(m.theme1==1)
            a.theme(Theme.DARK);
        a.title(getString(m, R.string.enterzipname));
        a.positiveText(R.string.create);
        a.positiveColor(Color.parseColor(m.fabskin));
        a.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog materialDialog) {
                String name = current + "/" + materialDialog.getInputEditText().getText().toString();
                m.compressFiles(new File(name), b);
            }

            @Override
            public void onNegative(MaterialDialog materialDialog) {

            }
        });
        a.negativeText(getString(m, R.string.cancel));
        a.negativeColor(Color.parseColor(m.fabskin));
        a.build().show();
    }

    public void showSortDialog(final Main m) {
        String[] sort = m.getResources().getStringArray(R.array.sortby);
        int current = Integer.parseInt(m.Sp.getString("sortby", "0"));
        MaterialDialog.Builder a = new MaterialDialog.Builder(m.getActivity());
        if(m.theme1==1)a.theme(Theme.DARK);
        a.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                m.Sp.edit().putString("sortby", "" + which).commit();
                m.getSortModes();
                m.updateList();
                dialog.dismiss();
                return true;
            }
        });
        a.title(R.string.sortby);
        a.build().show();
    }

    public void showSortDialog(final AppsList m) {
        String[] sort = m.getResources().getStringArray(R.array.sortbyApps);
        int current = Integer.parseInt(m.Sp.getString("sortbyApps", "0"));
        MaterialDialog.Builder a = new MaterialDialog.Builder(m.getActivity());
        if(m.theme1==1)a.theme(Theme.DARK);
        a.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                m.Sp.edit().putString("sortbyApps", "" + which).commit();
                m.getSortModes();
                m.loadlist(false);
                dialog.dismiss();
                return true;
            }
        });
        a.title(R.string.sortby);
        a.build().show();
    }

    public void showHistoryDialog(final Main m) {
        final ArrayList<String> paths = m.history.readTable();
        final MaterialDialog.Builder a = new MaterialDialog.Builder(m.getActivity());
        a.positiveText(R.string.cancel);
        a.positiveColor(Color.parseColor(m.fabSkin));
        a.title(R.string.history);
        if(m.theme1==1)
            a.theme(Theme.DARK);

        a.autoDismiss(true);
        HiddenAdapter adapter = new HiddenAdapter(m.getActivity(),m, R.layout.bookmarkrow, toFileArray(paths),m.hidden,null,true);
        a.adapter(adapter, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {

            }
        });

        MaterialDialog x= a.build();
        adapter.updateDialog(x);
        x.show();

    }

    public void showHiddenDialog(final Main m) {
          final ArrayList<String> paths = m.hidden.readTable();
            final MaterialDialog.Builder a = new MaterialDialog.Builder(m.getActivity());
        a.positiveText(R.string.cancel);
        a.positiveColor(Color.parseColor(m.fabSkin));
        a.title(R.string.hiddenfiles);
        if(m.theme1==1)
            a.theme(Theme.DARK);
        a.autoDismiss(true);
        HiddenAdapter adapter = new HiddenAdapter(m.getActivity(),m, R.layout.bookmarkrow, toFileArray(paths),m.hidden,null,false);
        a.adapter(adapter, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {

            }
        });
        MaterialDialog x= a.build();
        adapter.updateDialog(x);
        x.show();

    }

    public void setPermissionsDialog(final Layoutelements f, final Main main){
        if(main.rootMode){
            final File file=new File(f.getDesc());
            final MaterialDialog.Builder a=new MaterialDialog.Builder(main.getActivity());
            View v=main.getActivity().getLayoutInflater().inflate(R.layout.permissiontable,null);
            final CheckBox readown=(CheckBox) v.findViewById(R.id.creadown);
            final CheckBox readgroup=(CheckBox) v.findViewById(R.id.creadgroup);
            final CheckBox readother=(CheckBox) v.findViewById(R.id.creadother);
            final CheckBox writeown=(CheckBox) v.findViewById(R.id.cwriteown);
            final CheckBox writegroup=(CheckBox) v.findViewById(R.id.cwritegroup);
            final CheckBox writeother=(CheckBox) v.findViewById(R.id.cwriteother);
            final CheckBox exeown=(CheckBox) v.findViewById(R.id.cexeown);
            final CheckBox exegroup=(CheckBox) v.findViewById(R.id.cexegroup);
            final CheckBox exeother=(CheckBox) v.findViewById(R.id.cexeother);
            String perm=f.getPermissions();
            if(perm.length()<6){
                Toast.makeText(main.getActivity(),R.string.not_allowed,Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<Boolean[]> arrayList=parse(perm);
            Boolean[] read=arrayList.get(0);
            Boolean[] write=arrayList.get(1);
            Boolean[] exe=arrayList.get(2);
            readown.setChecked(read[0]);
            readgroup.setChecked(read[1]);
            readother.setChecked(read[2]);
            writeown.setChecked(write[0]);
            writegroup.setChecked(write[1]);
            writeother.setChecked(write[2]);
            exeown.setChecked(exe[0]);
            exegroup.setChecked(exe[1]);
            exeother.setChecked(exe[2]);
            a.positiveText(R.string.set);
            a.positiveColor(Color.parseColor(main.fabSkin));
            a.widgetColor(Color.parseColor(main.fabSkin));
            a.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog materialDialog) {
                    int a=0,b=0,c=0;
                    if(readown.isChecked())a=4;
                    if(writeown.isChecked())b=2;
                    if(exeown.isChecked())c=1;
                    int owner=a+b+c;
                    int d=0,e=0,f=0;
                    if(readgroup.isChecked())d=4;
                    if(writegroup.isChecked())e=2;
                    if(exegroup.isChecked())f=1;
                    int group=d+e+f;
                    int g=0,h=0,i=0;
                    if(readother.isChecked())g=4;
                    if(writeother.isChecked())h=2;
                    if(exeother.isChecked())i=1;
                    int other=g+h+i;
                    String  finalValue=owner+""+group+""+other;

                    String command="chmod "+finalValue+" "+file.getPath();
                    if(file.isDirectory())command="chmod -R "+finalValue+" "+file.getPath();
                    Command com=new Command(1,command) {
                        @Override
                        public void commandOutput(int i, String s) {
                            Toast.makeText(main.getActivity(),s,Toast.LENGTH_LONG);
                        }

                        @Override
                        public void commandTerminated(int i, String s) {
                            Toast.makeText(main.getActivity(),s,Toast.LENGTH_LONG);
                        }

                        @Override
                        public void commandCompleted(int i, int i2) {
                            Toast.makeText(main.getActivity(),main.getResources().getString(R.string.done),Toast.LENGTH_LONG);
                        }
                    };
                    try {//
                        RootTools.remount(file.getPath(),"RW");
                        RootTools.getShell(true).add(com);
                        main.updateList();
                    } catch (Exception e1) {
                        Toast.makeText(main.getActivity(),main.getResources().getString(R.string.error),Toast.LENGTH_LONG).show();
                        e1.printStackTrace();
                    }

                }

                @Override
                public void onNegative(MaterialDialog materialDialog) {

                }
            });
            a.title(file.getName());
            a.customView(v, true);
            if(main.theme1==1)a.theme(Theme.DARK);
            a.build().show();}else{Toast.makeText(main.getActivity(),main.getResources().getString(R.string.enablerootmde),Toast.LENGTH_LONG).show();}
    }

    public String[] parseName(String line) {
        boolean linked = false;
        String name = "", link = "", size = "-1", date = "";
        String[] array = line.split(" ");
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains("->")) {
                linked = true;
            }
        }
        int p = getColonPosition(array);
        date = array[p - 1] + " | " + array[p];
        size = array[p - 2];
        if (!linked) {
            for (int i = p + 1; i < array.length; i++) {
                name = name + " " + array[i];
            }
            name = name.trim();
        } else {
            int q = getLinkPosition(array);
            for (int i = p + 1; i < q; i++) {
                name = name + " " + array[i];
            }
            name = name.trim();
            for (int i = q + 1; i < array.length; i++) {
                link = link + " " + array[i];
            }
        }
        String size1 = size;
        if (size.equals("")) {
            size = "-1";
            size1 = "";
        }
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd | HH:mm");
        Date stringDate = simpledateformat.parse(date, pos);
        return new String[]{name, link, array[0], size, stringDate.getTime() + "", size1};
    }

    public int getLinkPosition(String[] array){
        for(int i=0;i<array.length;i++){
            if(array[i].contains("->"))return i;
        }
        return  0;
    }

public int getColonPosition(String[] array){
        for(int i=0;i<array.length;i++){
            if(array[i].contains(":"))return i;
        }
        return  0;
    }

    public ArrayList<Boolean[]> parse(String permLine) {
        ArrayList<Boolean[]> arrayList=new ArrayList<Boolean[]>();
        Boolean[] read=new Boolean[]{false,false,false};
        Boolean[] write=new Boolean[]{false,false,false};
        Boolean[] execute=new Boolean[]{false,false,false};
        int owner = 0;
        if (permLine.charAt(1) == 'r') {
            owner += READ;
            read[0]=true;
        }
        if (permLine.charAt(2) == 'w') {
            owner += WRITE;
            write[0]=true;
        }
        if (permLine.charAt(3) == 'x') {
            owner += EXECUTE;
            execute[0]=true;
        }
        int group = 0;
        if (permLine.charAt(4) == 'r') {
            group += READ;
            read[1]=true;
        }
        if (permLine.charAt(5) == 'w') {
            group += WRITE;
            write[1]=true;
        }
        if (permLine.charAt(6) == 'x') {
            group += EXECUTE;
        execute[1]=true;
        }
        int world = 0;
        if (permLine.charAt(7) == 'r') {
            world += READ;
        read[2]=true;
        }
        if (permLine.charAt(8) == 'w') {
            world += WRITE;
        write[2]=true;
        }
        if (permLine.charAt(9) == 'x') {
            world += EXECUTE;
        execute[2]=true;
        }
        arrayList.add(read);
        arrayList.add(write);
        arrayList.add(execute);
        return arrayList;
    }
}
