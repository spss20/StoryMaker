package com.ssoftwares.lublu.utils;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdInputFilter implements InputFilter {
    Pattern mPattern;
    Context mContext;
    public IdInputFilter(Context context, String regex) {
        mPattern = Pattern.compile(regex);
        mContext = context;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Matcher matcher = mPattern.matcher(source);
        if (!matcher.matches()) {
            return "";
        }
        return null;
    }
}
