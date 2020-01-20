package com.yixin.tinode.ui.presenter;

import com.yixin.tinode.app.MyApp;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IMainAtView;
import com.yixin.tinode.util.UIUtils;


public class MainAtPresenter extends BasePresenter<IMainAtView> {

	public MainAtPresenter(BaseActivity context) {
		super(context);
//        connect(UserCache.getToken());
		//同步所有用户信息
//        DBManager.getInstance().getAllUserInfo();
	}


	/**
	 * 建立与融云服务器的连接
	 *
	 * @param token
	 */
	private void connect(String token) {

		if (UIUtils.getContext().getApplicationInfo().packageName.equals(MyApp.getCurProcessName(UIUtils.getContext()))) {

		}
	}
}
