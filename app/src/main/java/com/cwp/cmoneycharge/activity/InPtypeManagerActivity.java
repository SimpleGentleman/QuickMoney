package com.cwp.cmoneycharge.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cwp.chart.manager.SystemBarTintManager;
import com.cwp.cmoneycharge.R;
import com.cwp.cmoneycharge.app.SysApplication;

import java.util.ArrayList;
import java.util.List;

import cwp.moneycharge.dao.ItypeDAO;
import cwp.moneycharge.dao.PtypeDAO;
import cwp.moneycharge.model.Tb_itype;
import cwp.moneycharge.model.Tb_ptype;
import cwp.moneycharge.widget.CustomDialog;

/**
 * 收入类型和支出类型的管理的界面
 */
public class InPtypeManagerActivity extends Activity {

	private List<String> typename;
	private ListView lv;
	int userid, type;
	Intent intentr;
	ItypeDAO itypeDAO;
	PtypeDAO ptypeDAO;
	TextView inptext;
	Button add, delete;
	String inputStr = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inptypemanager);

		SystemBarTintManager mTintManager;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			findViewById(R.id.inptext_top).setVisibility(View.VISIBLE);
			setTranslucentStatus(true);
		}
		mTintManager = new SystemBarTintManager(this);
		mTintManager.setStatusBarTintEnabled(true);
		mTintManager.setStatusBarTintResource(R.color.statusbar_bg);

		SysApplication.getInstance().addActivity(this); // 在销毁队列中添加this
		inptext = (TextView) findViewById(R.id.inptext);
		lv = (ListView) findViewById(R.id.typelist);// 得到ListView对象的引用
		add = (Button) findViewById(R.id.addtype);
		delete = (Button) findViewById(R.id.deletetype);
	}

	//设置半透明的状态
	@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	@Override
	public void onStart() {
		super.onStart();
		// 获取数据
		intentr = getIntent();
		userid = intentr.getIntExtra("cwp.id", 100000001);
		type = intentr.getIntExtra("type", 0);
		itypeDAO = new ItypeDAO(InPtypeManagerActivity.this);
		ptypeDAO = new PtypeDAO(InPtypeManagerActivity.this);
		if (type == 0) {
			typename = itypeDAO.getItypeName(userid);
			inptext.setText("收入类型管理");
		} else {
			typename = ptypeDAO.getPtypeName(userid);
			inptext.setText("支出类型管理");
		}

		/* 为ListView设置Adapter来绑定数据 */
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_checked, typename));

		//点击按钮的点击事件
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				inputTitleDialog();
			}

		});
		//删除按钮的点击事件
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				deleteDialog();
			}
		});
	}

	private void inputTitleDialog() {

		final EditText inputServer = new EditText(InPtypeManagerActivity.this);
		inputServer.setFocusable(true);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("添加类型").setView(inputServer)
				.setNegativeButton("取消", null);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				inputStr = inputServer.getText().toString();
				int i = (int) itypeDAO.getCount(userid) + 1;
				if (inputStr.trim().equals("")) {
					Toast.makeText(InPtypeManagerActivity.this, "输入内容不能为空！",
							Toast.LENGTH_LONG).show();
					refresh();
				} else if (type == 0) {
					itypeDAO.add(new Tb_itype(userid, i, inputStr));
				} else {
					ptypeDAO.add(new Tb_ptype(userid, i, inputStr));
				}
				refresh();
			}
		});
		builder.show();
	}

	private void deleteDialog() { // 退出程序的方法
		Dialog dialog = null;

		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				InPtypeManagerActivity.this);

		customBuilder
				.setTitle("删除")
				// 创建标题

				.setMessage("您确定要删除吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						onDeleteClick();
						Toast.makeText(InPtypeManagerActivity.this, "已删除！",
								Toast.LENGTH_LONG).show();
						dialog.dismiss();
						refresh();
					}

				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});
		dialog = customBuilder.create();// 创建对话框
		dialog.show(); // 显示对话框

	}

	public void onDeleteClick() {
		// 获取选中的行
		SparseBooleanArray checked = lv.getCheckedItemPositions();
		List<String> checkList = new ArrayList<String>();
		for (int i = 0; i < lv.getCount(); i++) {
			if (checked.get(i) == true) {
				// 获取到选择的行的数据
				checkList.add(typename.get(i).toString());
			}
		}
		if (checkList.size() > 0) {
			for (String lchecked : checkList) {
				if (type == 1)
					ptypeDAO.deleteByName(userid, lchecked);
				else
					itypeDAO.deleteByName(userid, lchecked);
			}
		} else {
			Toast.makeText(InPtypeManagerActivity.this, "您未选中任何项,请选择",
					Toast.LENGTH_LONG).show();
		}

		lv.clearChoices();// 清空listView的选择状态，方便下次选择
	}

	public void refresh() {
		finish();
		Intent intentf = new Intent(InPtypeManagerActivity.this, InPtypeManagerActivity.class);
		intentf.putExtra("cwp.id", userid);
		intentf.putExtra("type", type);
		startActivity(intentf);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控/拦截/屏蔽返回键
			Intent intent = new Intent(InPtypeManagerActivity.this, MainActivity.class);
			intent.putExtra("cwp.id", userid);
			intent.putExtra("cwp.Fragment", "4");// 设置传递数据
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
