package com.solulab.libs.helper


import android.content.Context
import android.support.v7.widget.AppCompatSpinner
import android.util.AttributeSet

class CustomSpinner : AppCompatSpinner {
    internal var context: Context? = null

    constructor(context: Context) : super(context) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}