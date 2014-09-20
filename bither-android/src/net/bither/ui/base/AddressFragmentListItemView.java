/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.ui.base;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.bither.BitherSetting;
import net.bither.R;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.Utils;
import net.bither.ui.base.dialog.DialogAddressFull;
import net.bither.util.CurrencySymbolUtil;
import net.bither.util.GenericUtils;
import net.bither.util.StringUtil;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;

public class AddressFragmentListItemView extends FrameLayout implements
        AddressInfoChangedObserver, MarketTickerChangedObserver {

    private FragmentActivity activity;
    private TextView tvAddress;
    private TextView tvBalance;
    private ImageView ivBalanceSymbol;
    private BtcToMoneyTextView tvBalanceMoney;
    public ImageView ivWatchOnlyType;
    public ImageView ivPrivateType;
    private ImageView ivXRandomLabel;
    private TransactionImmutureSummeryListItemView vTransactionImmuture;
    private View llExtra;
    private TextView tvTransactionCount;
    private View llMonitorFailed;
    private Address address;

    public AddressFragmentListItemView(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
        View v = LayoutInflater.from(activity).inflate(R.layout.list_item_address_fragment_warm,
                null);
        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        initView();
    }

    private void initView() {
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvBalance = (TextView) findViewById(R.id.tv_balance);
        ivBalanceSymbol = (ImageView) findViewById(R.id.iv_balance_symbol);
        tvTransactionCount = (TextView) findViewById(R.id.tv_transaction_count);
        llExtra = findViewById(R.id.ll_extra);
        llMonitorFailed = findViewById(R.id.ll_monitor_failed);
        tvBalanceMoney = (BtcToMoneyTextView) findViewById(R.id.tv_balance_money);
        ivWatchOnlyType = (ImageView) findViewById(R.id.iv_type_watchonly);
        ivPrivateType = (ImageView) findViewById(R.id.iv_type_private);
        ivXRandomLabel = (ImageView) findViewById(R.id.iv_xrandom_label);
        ivBalanceSymbol.setImageBitmap(CurrencySymbolUtil.getBtcSlimSymbol(tvBalance));
        findViewById(R.id.ibtn_address_full).setOnClickListener(addressFullClick);
        vTransactionImmuture = (TransactionImmutureSummeryListItemView) findViewById(R.id
                .v_transaction_immuture);
        vTransactionImmuture.setActivity(activity);
    }

    private AddressFragmentListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private AddressFragmentListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAddress(Address address, int loaderPosition, boolean isPrivate) {
        this.address = address;

        if (address != null) {
            showAddressInfo();
        }
    }

    private void showAddressInfo() {
        if (address == null) {
            return;
        }
        tvAddress.setText(address.getShortAddress());
        tvBalanceMoney.setVisibility(View.VISIBLE);
        ivBalanceSymbol.setVisibility(View.VISIBLE);
        llExtra.setVisibility(View.VISIBLE);
        llMonitorFailed.setVisibility(View.GONE);
        tvTransactionCount.setVisibility(View.GONE);
        if (address.hasPrivKey()) {
            ivWatchOnlyType.setVisibility(GONE);
            ivPrivateType.setVisibility(VISIBLE);
        } else {
            ivWatchOnlyType.setVisibility(VISIBLE);
            ivPrivateType.setVisibility(GONE);
        }
        if (this.address != null && this.address.isSyncComplete()) {
            tvBalance.setText(GenericUtils.formatValueWithBold(this.address.getBalance()));
            tvBalanceMoney.setBigInteger(BigInteger.valueOf(this.address.getBalance()));
            tvTransactionCount.setText(Integer.toString(this.address.txCount()));

            Tx lastTransaction = null;
            if (this.address.txCount() > 0) {
                List<Tx> txList = this.address.getRecentlyTxsWithConfirmationCntLessThan(6, 1);
                if (txList.size() > 0) {
                    lastTransaction = txList.get(0);
                }
            }
            if (lastTransaction != null && lastTransaction.getConfirmationCount() < 6) {
                vTransactionImmuture.setVisibility(View.VISIBLE);
                vTransactionImmuture.setTransaction(lastTransaction, address);
            } else {
                vTransactionImmuture.setVisibility(View.GONE);
            }
            if (vTransactionImmuture.getVisibility() == View.GONE) {
                tvTransactionCount.setVisibility(View.VISIBLE);
            }
        } else {
            ivBalanceSymbol.setVisibility(View.GONE);
            tvBalance.setText(BitherSetting.UNKONW_ADDRESS_STRING);
            tvBalanceMoney.setBigInteger(null);
            vTransactionImmuture.setVisibility(View.GONE);
//            if (address.isError()) {
//                llExtra.setVisibility(View.GONE);
//                llMonitorFailed.setVisibility(View.VISIBLE);
//            }
        }
    }


    @Override
    public void onMarketTickerChanged() {
        tvBalanceMoney.onMarketTickerChanged();
    }

    @Override
    public void onAddressInfoChanged(String address) {
        if (Utils.compareString(address, this.address.getAddress())) {
            showAddressInfo();
        }
    }

    private OnClickListener addressFullClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>();
            map.put(address.getAddress(), 0L);
            DialogAddressFull dialog = new DialogAddressFull(activity, map);
            dialog.show(v);
        }
    };

    public void onPause() {
        vTransactionImmuture.onPause();
    }

    public void onResume() {
        showAddressInfo();
        vTransactionImmuture.onResume();
    }
}
