package com.hefny.hady.blogpost.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.hefny.hady.blogpost.R
import com.hefny.hady.blogpost.models.AccountProperties
import com.hefny.hady.blogpost.ui.main.account.state.AccountStateEvent
import kotlinx.android.synthetic.main.fragment_update_account.*

class UpdateAccountFragment : BaseAccountFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                Log.d(TAG, "UpdateAccountFragment: $dataState")
            }
        })
        viewModel.viewState.observe(viewLifecycleOwner, Observer { accountViewState ->
            accountViewState.accountProperties?.let { accountProperties ->
                Log.d(TAG, "UpdateAccountFragment, viewState: $accountProperties")
                setAccountDataFields(accountProperties)
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        input_email?.let {
            input_email.setText(accountProperties.email)
        }
        input_username?.let {
            input_username.setText(accountProperties.username)
        }
    }

    private fun saveChanges() {
        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountPropertiesEvent(
                input_email.text.toString(),
                input_username.text.toString()
            )
        )
        keyboardManagement.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}