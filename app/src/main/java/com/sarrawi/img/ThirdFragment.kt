package com.sarrawi.img
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sarrawi.img.Api.ApiService
import com.sarrawi.img.adapter.ImgAdapter
import com.sarrawi.img.databinding.FragmentThirdBinding
import com.sarrawi.img.db.repository.ImgRepository
import com.sarrawi.img.db.viewModel.Imgs_ViewModel
import com.sarrawi.img.db.viewModel.ViewModelFactory
import kotlinx.coroutines.launch


class ThirdFragment : Fragment() {

    private lateinit var _binding: FragmentThirdBinding
    private val binding get() = _binding
    private val retrofitService = ApiService.provideRetrofitInstance()
    private val mainRepository by lazy { ImgRepository(retrofitService) }
    private val imgsViewModel: Imgs_ViewModel by viewModels {
        ViewModelFactory(requireContext(), mainRepository)
    }
    private val imgAdapter by lazy { ImgAdapter(requireActivity()) }
    private var ID_Type_id = -1
    private var recyclerViewState: Parcelable? = null
    private var customScrollState = CustomScrollState()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ID_Type_id = ThirdFragmentArgs.fromBundle(requireArguments()).id
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // حفظ حالة التمرير
        recyclerViewState = binding.rvImgCont.layoutManager?.onSaveInstanceState()
        outState.putParcelable("recycler_state", recyclerViewState)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let { bundle ->
            // استعادة حالة التمرير
            recyclerViewState = bundle.getParcelable("recycler_state")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = binding.rvImgCont.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.scrollToPosition(customScrollState.scrollPosition)
        }

        imgsViewModel.isConnected.observe(requireActivity()) { isConnected ->
            if (isConnected) {
                setUpRv()
                adapterOnClick()
                imgAdapter.updateInternetStatus(isConnected)
                binding.lyNoInternet.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.lyNoInternet.visibility = View.VISIBLE
                imgAdapter.updateInternetStatus(isConnected)
            }
        }

        imgsViewModel.checkNetworkConnection(requireContext())

        imgsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = binding.rvImgCont.layoutManager
        if (layoutManager is LinearLayoutManager) {
            customScrollState.scrollPosition = layoutManager.findFirstVisibleItemPosition()
        }
    }

    private fun setUpRv() = lifecycleScope.launch {
        imgsViewModel.getAllImgsViewModel(ID_Type_id).observe(requireActivity()) { imgs ->
            imgAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

            imgAdapter.img_list = imgs

            if (binding.rvImgCont.adapter == null) {
                binding.rvImgCont.layoutManager = GridLayoutManager(requireContext(), 3)
                binding.rvImgCont.adapter = imgAdapter
            } else {
                imgAdapter.notifyDataSetChanged()
            }

            imgAdapter.onItemClick = { _, currentItemId ->
                if (imgsViewModel.isConnected.value == true) {
                    val directions = ThirdFragmentDirections.actionToFourFragment(ID_Type_id, currentItemId)
                    findNavController().navigate(directions)
                } else {
                    val snackbar = Snackbar.make(
                        requireView(),
                        "لا يوجد اتصال بالإنترنت",
                        Snackbar.LENGTH_SHORT
                    )
                    snackbar.show()
                }
            }
        }
    }

    private fun adapterOnClick() {
        imgAdapter.onItemClick = { _, currentItemId ->
            if (imgsViewModel.isConnected.value == true) {
                val directions = ThirdFragmentDirections.actionToFourFragment(ID_Type_id, currentItemId)
                findNavController().navigate(directions)
            } else {
                val snackbar = Snackbar.make(
                    requireView(),
                    "لا يوجد اتصال بالإنترنت",
                    Snackbar.LENGTH_SHORT
                )
                snackbar.show()
            }
        }
    }
}


//package com.sarrawi.img
//
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.os.Parcelable
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.viewModelScope
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.snackbar.Snackbar
//import com.sarrawi.img.Api.ApiService
//import com.sarrawi.img.adapter.ImgAdapter
//import com.sarrawi.img.databinding.FragmentThirdBinding
//import com.sarrawi.img.db.repository.ImgRepository
//import com.sarrawi.img.db.viewModel.Imgs_ViewModel
//import com.sarrawi.img.db.viewModel.ViewModelFactory
//import kotlinx.coroutines.launch
//
//class ThirdFragment : Fragment() {
//    private var scrollPosition = 0
//
//    private var isDataLoaded = false
//    private lateinit var _binding: FragmentThirdBinding
//    private val binding get() = _binding
//    private val retrofitService = ApiService.provideRetrofitInstance()
//    private val mainRepository by lazy { ImgRepository(retrofitService) }
//    private val imgsViewModel: Imgs_ViewModel by viewModels {
//        ViewModelFactory(requireContext(), mainRepository)
//    }
//    private var isInternetConnected: Boolean = true
//    private val imgAdapter by lazy { ImgAdapter(requireActivity()) }
//    private var ID_Type_id = -1
//    private var recyclerViewState: Parcelable? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentThirdBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        ID_Type_id = ThirdFragmentArgs.fromBundle(requireArguments()).id
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Log.d("MyApp", "تم استدعاء onSaveInstanceState")
//        outState.putParcelable("recycler_state",binding.rvImgCont.layoutManager?.onSaveInstanceState())
//        outState.putInt("scroll_position", scrollPosition)
//
//
//
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        savedInstanceState?.let { bundle ->
//            val recyclerViewState = bundle.getParcelable<Parcelable>("recycler_state")
//            binding.rvImgCont.layoutManager?.onRestoreInstanceState(recyclerViewState)
//        }
//        scrollPosition = savedInstanceState?.getInt("scroll_position") ?: 0
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        recyclerViewState?.let { state ->
//            binding.rvImgCont.layoutManager?.onRestoreInstanceState(state)
//        }
//
//        imgsViewModel.isConnected.observe(requireActivity()) { isConnected ->
//            if (isConnected) {
//                setUpRv()
//                adapterOnClick()
//                imgAdapter.updateInternetStatus(isConnected)
//                binding.lyNoInternet.visibility = View.GONE
//            } else {
//                binding.progressBar.visibility = View.GONE
//                binding.lyNoInternet.visibility = View.VISIBLE
//                imgAdapter.updateInternetStatus(isConnected)
//            }
//        }
//
//        imgsViewModel.checkNetworkConnection(requireContext())
//
//        imgsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            if (isLoading) {
//                binding.progressBar.visibility = View.VISIBLE
//            } else {
//                binding.progressBar.visibility = View.GONE
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        recyclerViewState = binding.rvImgCont.layoutManager?.onSaveInstanceState()
//    }
//
//    private fun setUpRv() = imgsViewModel.viewModelScope.launch {
//        imgsViewModel.getAllImgsViewModel(ID_Type_id).observe(requireActivity()) { imgs ->
//            imgAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
//            binding.rvImgCont.layoutManager?.scrollToPosition(scrollPosition)
//
//            imgAdapter.img_list = imgs
//
//            if (binding.rvImgCont.adapter == null) {
//                binding.rvImgCont.layoutManager = GridLayoutManager(requireContext(), 3)
//                binding.rvImgCont.adapter = imgAdapter
//            } else {
//                imgAdapter.notifyDataSetChanged()
//            }
//
//            val handler = Handler(Looper.getMainLooper())
//            handler.postDelayed({
//                hideProgressDialog()
//            }, 5000)
//
//            imgAdapter.onItemClick = { _, currentItemId ->
//                if (imgsViewModel.isConnected.value == true) {
//                    Log.d("MyApp", "تم النقر على العنصر: $currentItemId")
//                    val directions = ThirdFragmentDirections.actionToFourFragment(ID_Type_id, currentItemId)
//                    findNavController().navigate(directions)
//                } else {
//                    val snackbar = Snackbar.make(
//                        requireView(),
//                        "لا يوجد اتصال بالإنترنت",
//                        Snackbar.LENGTH_SHORT
//                    )
//                    snackbar.show()
//                }
//            }
//            binding.rvImgCont.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    // حفظ موقع التمرير الحالي هنا
//                    scrollPosition = (binding.rvImgCont.layoutManager as? GridLayoutManager)?.findFirstVisibleItemPosition() ?: 0
//                }
//            })
//
//
//            if (binding.rvImgCont.isSaveEnabled) {
//                Log.d("MyApp", "حالة حفظ التمرير مفعلة")
//            } else {
//                Log.d("MyApp", "حالة حفظ التمرير معطلة")
//            }
//        }
//    }
//
//    private fun adapterOnClick() {
//        imgAdapter.onItemClick = { _, currentItemId ->
//            if (imgsViewModel.isConnected.value == true) {
//                Log.d("MyApp", "تم النقر على العنصر: $currentItemId")
//                val directions = ThirdFragmentDirections.actionToFourFragment(ID_Type_id, currentItemId)
//                findNavController().navigate(directions)
//            } else {
//                val snackbar = Snackbar.make(
//                    requireView(),
//                    "لا يوجد اتصال بالإنترنت",
//                    Snackbar.LENGTH_SHORT
//                )
//                snackbar.show()
//            }
//        }
//    }
//
//    private fun hideProgressDialog() {
//        binding.progressBar.visibility = View.GONE
//        binding.lyNoInternet.visibility = View.GONE
//    }
//}

//package com.sarrawi.img
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.viewModelScope
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.sarrawi.img.Api.ApiService
//import com.sarrawi.img.adapter.ImgAdapter
//import com.sarrawi.img.db.repository.ImgRepository
//import com.sarrawi.img.db.viewModel.Imgs_ViewModel
//import com.sarrawi.img.db.viewModel.ViewModelFactory
//import kotlinx.coroutines.launch
//import android.os.Handler
//import android.os.Looper
//import android.os.Parcelable
//import android.util.Log
//import android.widget.Toast
//import androidx.navigation.fragment.findNavController
//import com.google.android.material.snackbar.Snackbar
//import com.sarrawi.img.databinding.FragmentThirdBinding
//
//class ThirdFragment : Fragment() {
//
//    private var isDataLoaded = false
//
//
//    private lateinit var _binding: FragmentThirdBinding
//
//    private val binding get() = _binding
//
//    private val retrofitService = ApiService.provideRetrofitInstance()
//
//    private val mainRepository by lazy {  ImgRepository(retrofitService) }
//
//    private val imgsViewmodel: Imgs_ViewModel by viewModels {
//        ViewModelFactory(requireContext(),mainRepository)
//    }
//
//    private var isInternetConnected: Boolean = true
//
//    private val imgAdapter by lazy {
//        ImgAdapter(requireActivity())
//    }
//
//    private var ID_Type_id = -1
//    private var scrollPosition: Parcelable? = null
//    private var recyclerViewState: Parcelable? = null
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        _binding = FragmentThirdBinding.inflate(inflater, container, false)
//        return binding.root
//
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        ID_Type_id = ThirdFragmentArgs.fromBundle(requireArguments()).id
//
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Log.d("MyApp", "تم استدعاء onSaveInstanceState")
//
//        outState.putParcelable("recycler_state", binding.rvImgCont.layoutManager?.onSaveInstanceState())
//        Log.d("MyApp", "تم استدعاء onSaveInstanceState")
//
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        savedInstanceState?.let { bundle ->
//            val recyclerViewState = bundle.getParcelable<Parcelable>("recycler_state")
//            binding.rvImgCont.layoutManager?.onRestoreInstanceState(recyclerViewState)
//        }
//    }
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        recyclerViewState?.let { state ->
//            binding.rvImgCont.layoutManager?.onRestoreInstanceState(state)
//        }
//
//        // Live Connected Check
////        imgsViewmodel.isConnected.observe(requireActivity()) { isConnected ->
////            if (isConnected){
////                setUpRv()
////                adapterOnClick()
////                 binding.lyNoInternet.visibility = View.GONE
////
////            }
////            else {
////                binding.progressBar.visibility = View.GONE
////                binding.lyNoInternet.visibility = View.VISIBLE
////
////             }
////
////
////        }
//
//        // في الـ ThirdFragment
//        imgsViewmodel.isConnected.observe(requireActivity()) { isConnected ->
//            if (isConnected) {
//                setUpRv()
//                adapterOnClick()
//                imgAdapter.updateInternetStatus(isConnected)
//                binding.lyNoInternet.visibility = View.GONE
//            } else {
//                binding.progressBar.visibility = View.GONE
//                binding.lyNoInternet.visibility = View.VISIBLE
//                imgAdapter.updateInternetStatus(isConnected)
//            }
//        }
//
//        imgsViewmodel.checkNetworkConnection(requireContext())
//
//        imgsViewmodel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            if (isLoading) {
//                binding.progressBar.visibility = View.VISIBLE // عرض ProgressBar إذا كان التحميل قيد التقدم
//            } else {
//                binding.progressBar.visibility = View.GONE // إخفاء ProgressBar إذا انتهى التحميل
//            }
//        }
//
//
//
//
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//
//    }
//
//
//    override fun onPause() {
//        super.onPause()
//        recyclerViewState = binding.rvImgCont.layoutManager?.onSaveInstanceState()
//    }
//
//
//
//
//
////
////    private fun setUpRv() = imgsViewmodel.viewModelScope.launch {
////        imgsViewmodel.getAllImgsViewModel(ID_Type_id).observe(requireActivity())
////        { imgs ->
////            imgAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
////
////            if (imgs != null) {
////                // print data
////                imgAdapter.img_list = imgs
////                if (binding.rvImgCont.adapter == null) {
////
////                    binding.rvImgCont.layoutManager = GridLayoutManager(requireContext(), 3)
////                    binding.rvImgCont.adapter = imgAdapter
////                    imgAdapter.notifyDataSetChanged()
////
////                } else {
////                    imgAdapter.notifyDataSetChanged()
////                }
////            }
////
////            else {
////                // imgs هي قائمة فارغة أو null، يمكنك اتخاذ الإجراء المناسب هنا
////            }
////
////            val handler = Handler(Looper.getMainLooper()) // تعريف handler هنا
////
////            handler.postDelayed({
////                hideprogressdialog()
////            }, 5000)
////        }
////    }
//
//
//    private fun setUpRvs() = imgsViewmodel.viewModelScope.launch {
//        imgsViewmodel.getAllImgsViewModel(ID_Type_id).observe(requireActivity())
//        { imgs ->
//            imgAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
//
//            // لاحظ أننا قمنا بحذف الشرط هنا وبدلاً من ذلك نقوم بتحديث البيانات مباشرة
//            imgAdapter.img_list = imgs
//
//            if (binding.rvImgCont.adapter == null) {
//                binding.rvImgCont.layoutManager = GridLayoutManager(requireContext(), 3)
//                binding.rvImgCont.adapter = imgAdapter
//            } else {
//                imgAdapter.notifyDataSetChanged()
//            }
//
//            val handler = Handler(Looper.getMainLooper()) // تعريف handler هنا
//
//            handler.postDelayed({
//                hideprogressdialog()
//            }, 5000)
//        }
//    }
//
//
//
//    private fun setUpRv() = imgsViewmodel.viewModelScope.launch {
//        imgsViewmodel.getAllImgsViewModel(ID_Type_id).observe(requireActivity())
//        { imgs ->
//            imgAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
//
//            // لاحظ أننا قمنا بحذف الشرط هنا وبدلاً من ذلك نقوم بتحديث البيانات مباشرة
//            imgAdapter.img_list = imgs
//
//            if (binding.rvImgCont.adapter == null) {
//                binding.rvImgCont.layoutManager = GridLayoutManager(requireContext(), 3)
//                binding.rvImgCont.adapter = imgAdapter
//            } else {
//                imgAdapter.notifyDataSetChanged()
//            }
//
//            val handler = Handler(Looper.getMainLooper()) // تعريف handler هنا
//
//            handler.postDelayed({
//                hideprogressdialog()
//            }, 5000)
//
//            // تعيين معالج النقر هنا
//            imgAdapter.onItemClick = { id, currentItemId ->
//                if (imgsViewmodel.isConnected.value == true) {
//                    // القم بتنفيذ الإجراء فقط إذا كان هناك اتصال بالإنترنت
//                    Log.d("MyApp", "تم النقر على العنصر: $currentItemId")
//                    val directions = ThirdFragmentDirections.actionToFourFragment(ID_Type_id, currentItemId)
//                    findNavController().navigate(directions)
//                } else {
//                    // إذا كان الاتصال بالإنترنت معطلًا، لا تفعيل النقر (onclick)
//                    // إذا كان الاتصال بالإنترنت معطلًا، قم بعرض رسالة Toast لتنبيه المستخدم
//                    val snackbar = Snackbar.make(
//                        requireView(), // يجب أن يكون View المرتبط بالشاشة الحالية
//                        "لا يوجد اتصال بالإنترنت",
//                        Snackbar.LENGTH_SHORT
//                    )
//                    snackbar.show()
//                }
//            }
//
//            // التحقق من حالة حفظ التمرير
//            if (binding.rvImgCont.isSaveEnabled ) {
//                // حالة حفظ التمرير مفعلة
//                Log.d("MyApp", "حالة حفظ التمرير مفعلة")
//            } else {
//                // حالة حفظ التمرير معطلة
//                Log.d("MyApp", "حالة حفظ التمرير معطلة")
//            }
//
//        }
//    }
//
//
//
//
//    fun hideprogressdialog() {
//        binding.progressBar.visibility = View.GONE
//        binding.lyNoInternet.visibility = View.GONE
//    }
//
////    fun adapterOnClick(){
////
////        imgAdapter.onItemClick = {id, currentItemId ->
////             // currentItemId for current item selected
////            //id not use
////            val directions = ThirdFragmentDirections.actionToFourFragment(ID_Type_id,currentItemId)
////            findNavController().navigate(directions,)
////        }
////    }
//
//    fun adapterOnClick(){
//        imgAdapter.onItemClick = { id, currentItemId ->
//            if (imgsViewmodel.isConnected.value==true) {
//                // القم بتنفيذ الإجراء فقط إذا كان هناك اتصال بالإنترنت
//                Log.d("MyApp", "تم النقر على العنصر: $currentItemId")
//                val directions = ThirdFragmentDirections.actionToFourFragment(ID_Type_id, currentItemId)
//                findNavController().navigate(directions)
//            } else {
//                // إذا كان الاتصال بالإنترنت معطلًا، لا تفعيل النقر (onclick)
//                // إذا كان الاتصال بالإنترنت معطلًا، قم بعرض رسالة Toast لتنبيه المستخدم
//                val snackbar = Snackbar.make(
//                    requireView(), // يجب أن يكون View المرتبط بالشاشة الحالية
//                    "لا يوجد اتصال بالإنترنت",
//                    Snackbar.LENGTH_SHORT
//                )
//                snackbar.show()
//            }
//        }
//    }
//
//
//
//}