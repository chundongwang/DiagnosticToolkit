
package com.microsoft.projecta.tools.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.AXmlResourceParser;
import android.util.TypedValue;

public class AndroidManifestInfo {
    private static final String ANDROIDMANIFEST_XML = "AndroidManifest.xml";

    private String mPackageName;
    private List<String> mActivities;
    private String mMainActivity;

    private AndroidManifestInfo() {
        mActivities = new ArrayList<String>();
    }

    private static String getNamespacePrefix(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return "";
        }
        return prefix + ":";
    }

    private static String getPackage(int id) {
        if (id >>> 24 == 1) {
            return "android:";
        }
        return "";
    }

    private static String getAttributeValue(AXmlResourceParser parser, int index) {
        int type = parser.getAttributeValueType(index);
        int data = parser.getAttributeValueData(index);
        if (type == TypedValue.TYPE_STRING) {
            return parser.getAttributeValue(index);
        }
        if (type == TypedValue.TYPE_ATTRIBUTE) {
            return String.format("?%s%08X", getPackage(data), data);
        }
        if (type == TypedValue.TYPE_REFERENCE) {
            return String.format("@%s%08X", getPackage(data), data);
        }
        if (type == TypedValue.TYPE_FLOAT) {
            return String.valueOf(Float.intBitsToFloat(data));
        }
        if (type == TypedValue.TYPE_INT_HEX) {
            return String.format("0x%08X", data);
        }
        if (type == TypedValue.TYPE_INT_BOOLEAN) {
            return data != 0 ? "true" : "false";
        }
        if (type == TypedValue.TYPE_DIMENSION) {
            return Float.toString(complexToFloat(data)) +
                    DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type == TypedValue.TYPE_FRACTION) {
            return Float.toString(complexToFloat(data)) +
                    FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type >= TypedValue.TYPE_FIRST_COLOR_INT && type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return String.format("#%08X", data);
        }
        if (type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT) {
            return String.valueOf(data);
        }
        return String.format("<0x%X, type 0x%02X>", data, type);
    }

    public static float complexToFloat(int complex) {
        return (float) (complex & 0xFFFFFF00) * RADIX_MULTS[(complex >> 4) & 3];
    }

    private static final float RADIX_MULTS[] = {
            0.00390625F, 3.051758E-005F, 1.192093E-007F, 4.656613E-010F
    };
    private static final String DIMENSION_UNITS[] = {
            "px", "dip", "sp", "pt", "in", "mm", "", ""
    };
    private static final String FRACTION_UNITS[] = {
            "%", "%p", "", "", "", "", "", ""
    };

    public static AndroidManifestInfo parseAndroidManifest(String apkPath) throws IOException,
            XmlPullParserException {
        ZipFile file = null;
        AndroidManifestInfo helper = null;
        try {
            AXmlResourceParser parser = new AXmlResourceParser();
            file = new ZipFile(new File(apkPath), ZipFile.OPEN_READ);
            parser.open(file.getInputStream(file.getEntry(ANDROIDMANIFEST_XML)));

            helper = new AndroidManifestInfo();

            int type;
            Stack<String> tags = new Stack<String>();
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_TAG: {
                        String tagName = tags.push(parser.getName());
                        if (tagName.equals("manifest")) {
                            // manifest for package
                            for (int i = 0, size = parser.getAttributeCount(); i != size; ++i) {
                                String attrName = getNamespacePrefix(parser.getAttributePrefix(i)) +
                                        parser.getAttributeName(i);
                                if (attrName.equals("package")) {
                                    helper.mPackageName = getAttributeValue(parser, i);
                                    break;
                                }
                            }
                        } else if (tagName.equals("activity")) {
                            // activity for android:name
                            for (int i = 0, size = parser.getAttributeCount(); i != size; ++i) {
                                String attrName = getNamespacePrefix(parser.getAttributePrefix(i)) +
                                        parser.getAttributeName(i);
                                if (attrName.equals("android:name")) {
                                    helper.mActivities.add(getAttributeValue(parser, i));
                                    break;
                                }
                            }
                        } else if (tagName.equals("action")) {
                            String androidName = null;
                            for (int i = 0, size = parser.getAttributeCount(); i != size; ++i) {
                                String attrName = getNamespacePrefix(parser.getAttributePrefix(i)) +
                                        parser.getAttributeName(i);
                                if (attrName.equals("android:name")) {
                                    androidName = getAttributeValue(parser, i);
                                    break;
                                }
                            }
                            if (androidName != null
                                    && androidName.equals("android.intent.action.MAIN")
                                    && tags.get(tags.size() - 2).equals("intent-filter")
                                    && tags.get(tags.size() - 3).equals("activity")) {
                                helper.mMainActivity = helper.mActivities.get(helper.mActivities
                                        .size() - 1);
                                break;
                            }
                        }
                        break;
                    }
                    case XmlPullParser.END_TAG: {
                        String tagName = tags.pop();
                        assert tagName == parser.getName();
                        break;
                    }
                }
            }
            parser.close();
        } finally {
            if (file != null) {
                file.close();
            }
        }

        return helper;
    }

    /**
     * @return the packageName
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * @return the mainActivity
     */
    public String getMainActivity() {
        return mMainActivity;
    }

    /**
     * @return the list of all activities
     */
    public List<String> getActivities() {
        return mActivities;
    }
}
