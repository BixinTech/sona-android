package cn.bixin.sona.demo.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initView()
        initViewModel()
    }

    abstract fun getLayoutId(): Int

    open fun initView() {

    }

    open fun initViewModel() {

    }
}