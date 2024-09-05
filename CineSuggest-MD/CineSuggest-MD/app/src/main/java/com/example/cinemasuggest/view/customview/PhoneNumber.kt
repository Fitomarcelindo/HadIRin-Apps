package com.example.cinemasuggest.view.customview

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import com.example.cinemasuggest.R

class PhoneNumber : TextEdit {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        // Set maximum length filter
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(15))

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!isValidPhoneNumber(s.toString())) {
                    error = resources.getString(R.string.error_phone_number)
                }
            }

            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
        })
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length <= 15 && phone.all { it.isDigit() }
    }
}
