package by.marcel.apps_lab.onboardingPage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import by.marcel.apps_lab.R
import by.marcel.apps_lab.adapter.CarouselAdapter
import by.marcel.apps_lab.home.HomeFirstActivity
import kotlin.random.Random


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class OnBoardPageFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView1: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var adapter1: CarouselAdapter
    private lateinit var adapter2: CarouselAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val delay = 3000L
    private val initialPosition = Integer.MAX_VALUE / 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_on_board_page, container, false)
        recyclerView1 = view.findViewById(R.id.RvCarousel1)
        recyclerView2 = view.findViewById(R.id.RvCarousel2)

        val images = listOf(R.drawable.fitto, R.drawable.nabila, R.drawable.lutfi, R.drawable.regi, R.drawable.adhit, R.drawable.amel, R.drawable.desi, R.drawable.testing_support)
        val shuffledImages1 = images.shuffled(Random(System.currentTimeMillis().toInt()))
        val shuffledImages2 = images.shuffled(Random(System.currentTimeMillis().toInt() + 1))

        adapter1 = CarouselAdapter(shuffledImages1)
        adapter2 = CarouselAdapter(shuffledImages2)

        recyclerView1.adapter = adapter1
        recyclerView1.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView1.scrollToPosition(initialPosition)
        disableUserScroll(recyclerView1)

        recyclerView2.adapter = adapter2
        val reverseLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false).apply {
            reverseLayout = true
        }
        recyclerView2.layoutManager = reverseLayoutManager
        recyclerView2.scrollToPosition(initialPosition)
        disableUserScroll(recyclerView2)

        startAutoSwipe()

        // Set text with different colors
        val tvMessage: TextView = view.findViewById(R.id.tv_message)
        val fullText = getString(R.string.onboarding1)
        val spannableString = SpannableString(fullText)
        val appsLabStart = fullText.indexOf("HadIRin Apps")
        val appsLabEnd = appsLabStart + "HadIRin Apps".length

        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE),
            appsLabStart,
            appsLabEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvMessage.text = spannableString

        // Set onClickListeners for Skip and Next buttons
        view.findViewById<View>(R.id.textViewSkip).setOnClickListener {
            navigateToLogin()
        }
        view.findViewById<View>(R.id.buttonNext).setOnClickListener {
            navigateToLogin()
        }

        return view

    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), HomeFirstActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun disableUserScroll(recyclerView: RecyclerView) {
        recyclerView.setOnTouchListener { _, _ -> true }
    }

    private fun startAutoSwipe() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val newPosition1 = (recyclerView1.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() + 1
                val newPosition2 = (recyclerView2.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() + 1
                smoothScrollToPosition(recyclerView1, newPosition1)
                smoothScrollToPosition(recyclerView2, newPosition2 - 2) // Adjust for reverse layout
                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    private fun smoothScrollToPosition(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                override fun getHorizontalSnapPreference(): Int {
                    return SNAP_TO_START
                }
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return 5000f / displayMetrics.densityDpi  // Increase this value for slower scrolling
                }
            }
            smoothScroller.targetPosition = position
            recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}