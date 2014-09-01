package util;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.appmoniter.R;

public class PackageInfoAdapter extends BaseAdapter {

	private LayoutInflater mlayoutInflater = null;
	private List<AppInfo> mpackageInfo = null;
	private boolean[] misSelected; 

	public PackageInfoAdapter(Context context, List<AppInfo> packageInfo, boolean[] isSelected) {
		super();
		// TODO Auto-generated constructor stub
		mlayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mpackageInfo = packageInfo;
		misSelected = isSelected;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mpackageInfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mpackageInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		View view = null;
		ViewHolder holder = null;

		if (convertView == null) {
			view = mlayoutInflater.inflate(R.layout.layout_list, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertView; 
			holder = (ViewHolder)view.getTag();
		}
		
		AppInfo appInfo = (AppInfo)getItem(position);
		holder.appIcon.setImageDrawable(appInfo.getAppIcon());
		holder.appPkgName.setText(appInfo.getPkgName());
		if(misSelected[position]){
			holder.isChooseButton.setSelected(true);
		}else{
			holder.isChooseButton.setSelected(false);
		}

		return view;
	}

	class ViewHolder {

		ImageView appIcon;
		TextView appPkgName;
		CheckBox isChooseButton;

		public ViewHolder(View view) {
			// TODO Auto-generated constructor stub
			this.appIcon = (ImageView) view.findViewById(R.id.appIcon);
			this.appPkgName = (TextView) view.findViewById(R.id.packagename);
			this.isChooseButton = (CheckBox)view.findViewById(R.id.isChoose);
		}
	}
}
