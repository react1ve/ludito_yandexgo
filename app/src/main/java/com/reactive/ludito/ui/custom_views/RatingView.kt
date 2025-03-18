package com.reactive.ludito.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.CENTER_VERTICAL
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.reactive.ludito.R
import com.reactive.premier.R as R2

class RatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val star1: ImageView
    private val star2: ImageView
    private val star3: ImageView
    private val star4: ImageView
    private val star5: ImageView
    private val feedbacks: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_rating, this, true)
        orientation = HORIZONTAL
        gravity = CENTER_VERTICAL

        star1 = findViewById(R.id.star1)
        star2 = findViewById(R.id.star2)
        star3 = findViewById(R.id.star3)
        star4 = findViewById(R.id.star4)
        star5 = findViewById(R.id.star5)
        feedbacks = findViewById(R.id.feedbacks)
    }

    fun setRating(rating: Float, feedbackCount: Int) {
        setStarColor(star1, rating, 1)
        setStarColor(star2, rating, 2)
        setStarColor(star3, rating, 3)
        setStarColor(star4, rating, 4)
        setStarColor(star5, rating, 5)

        feedbacks.text = context.getString(R.string.feedbacks, feedbackCount.toString())
    }

    private fun setStarColor(star: ImageView, rating: Float, starNumber: Int) {
        val colorRes = if (rating >= starNumber) R2.color.rating else R2.color.grey
        star.setColorFilter(ContextCompat.getColor(context, colorRes))
    }
}