package com.ranglerz.activity;
import java.util.List;

import com.ranglerz.utils.DataUtils;
import com.ranglerz.utils.ToastUtil;
import com.google.common.primitives.Bytes;
import com.hiklife.rfidapi.*;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.UHFHXAPI.Response;

public class TagWriteFragment extends Fragment {
	private Spinner spinnerArea;
	private TextView txtEpc;
	private EditText editInput;
	private TextView txtWarnning;
	private MyunmberinputSpinner unmpOffset;
	private MyunmberinputSpinner unmpLength;
	private EditText editAccesspwd;
	
	private static int circleCount;
	private boolean writeFlag;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.tag_write_layout, container,
				false);
		spinnerArea = (Spinner) rootView.findViewById(R.id.spinnerArea);
		String[] areas = new String[] { "EPC", "USER" };

		ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(
				rootView.getContext(),
				R.layout.simple_list_item, areas);
		areaAdapter
				.setDropDownViewResource(R.layout.simple_list_item);
		spinnerArea.setAdapter(areaAdapter);
		
		final Button buttonWrite = (Button)rootView.findViewById(R.id.buttonWrite);
		buttonWrite.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				buttonWrite.setClickable(false);
				if(getActivity() instanceof HKUHFActivity)
				{
					WriteTag();
				}
				else
				{
					writeFlag = true;
					circleCount = 10;
					while(writeFlag && 0 != circleCount)
					{
						write();
						circleCount--;
					}
					if(writeFlag)
					{
						ToastUtil.showToast(getActivity(), "写入失败！");
					}
					
				}
				buttonWrite.setClickable(true);
			}
		});
		
		txtEpc = (TextView) rootView.findViewById(R.id.txtWriteEpc);
		editInput = (EditText) rootView.findViewById(R.id.editInputInfo);
		txtWarnning = (TextView) rootView.findViewById(R.id.txtWarnning);
		unmpOffset = (MyunmberinputSpinner) rootView.findViewById(R.id.myunmberinputSpinner_offset);
		unmpLength = (MyunmberinputSpinner) rootView.findViewById(R.id.myunmberinputSpinner_length);
		editAccesspwd = (EditText) rootView.findViewById(R.id.editAccesspwd);
		
		return rootView;
    }
    
    public void WriteTag() {
    	try
    	{
    		txtWarnning.setText("");
    		
    		// 先进行标签选择操作
    		String maskValue = txtEpc.getText().toString();
    		if (maskValue.equals(getText(R.string.txt_null).toString()))
    		{
    			// 未选择标签
    			Toast.makeText(getActivity(),
						getText(R.string.info_no_sel_tag),
						Toast.LENGTH_SHORT).show();
    			return;
    		}

    		SingulationCriteria singulationCriteria = new SingulationCriteria();
            singulationCriteria.status = SingulationCriteriaStatus.Enabled;
            singulationCriteria.offset = 0;
            singulationCriteria.count = maskValue.length() * 4;
            singulationCriteria.match = matchType.Regular;

            for (int i = 0; i < (maskValue.length() / 2); i++ )
            {
            	singulationCriteria.mask[i] = (byte)(Short.parseShort(maskValue.substring(i * 2, i * 2 + 2), 16) & 0x00FF);
            }
            
            ctrlOperateResult result = ((HKUHFActivity)getActivity()).myRadio.Set18K6CPostMatchCriteria(singulationCriteria);
            if (result != ctrlOperateResult.OK)
            {
            	Toast.makeText(getActivity(),
            			result.toString(),
						Toast.LENGTH_SHORT).show();
    			return;
            }
    		
            // 进行标签写入操作
            // 先判断输入的数据是否合法
            if (Short.parseShort(unmpLength.getSelectedItem().toString()) == 0)
            {
            	Toast.makeText(getActivity(),
            			getText(R.string.info_write_length_zero),
						Toast.LENGTH_SHORT).show();
    			return;
            }
            
            String inputValue = editInput.getText().toString();
            if (Short.parseShort(unmpLength.getSelectedItem().toString()) != (inputValue.length() / 4))
            {
            	Toast.makeText(getActivity(),
            			getText(R.string.info_write_length_error),
						Toast.LENGTH_SHORT).show();
    			return;
            }
            
    		WriteParms parms = new WriteParms();
    		switch (spinnerArea.getSelectedItemPosition()) {
			case 0:
				parms.memBank = MemoryBank.EPC;
				break;
				
			case 1:
				parms.memBank = MemoryBank.USER;
				break;
				
			default:
				break;
			}
    		
    		parms.offset = Short.parseShort(unmpOffset.getSelectedItem().toString());
    		parms.length = Short.parseShort(unmpLength.getSelectedItem().toString());
    		parms.accesspassword = Integer.parseInt(editAccesspwd.getText().toString(), 16);
    		short[] writeBuf = new short[parms.length];
            for (int i = 0; i < writeBuf.length; i++)
            {
            	writeBuf[i] = (short)(Integer.parseInt(inputValue.substring(i * 4, i * 4 + 4), 16) & 0x0000FFFF);
            }

    		List<TagOperResult> tagInfos = ((HKUHFActivity)getActivity()).myRadio.TagInfoWrite(parms, writeBuf);
    		if (tagInfos.size() > 0)
    		{
    			// 因为已经锁定过标签，因此取一个进行显示
    			if (tagInfos.get(tagInfos.size() - 1).result == tagMemoryOpResult.Ok)
    			{
    				// 判断是否对EPC进行了改动，如果是则需要更新标签的EPC的显示，这样才能方便下次操作
    				if (parms.memBank == MemoryBank.EPC)
    				{
    					// 判断修改的部分是否在显示区域
    					if (parms.offset > 1 && parms.offset < maskValue.length() / 4 + 2)
    					{
    						int replaceLength = (parms.length * 4) > maskValue.length() ? maskValue.length() : (parms.length * 4);
    						maskValue = maskValue.substring(0, (parms.offset - 2) * 4) + inputValue.substring(0, replaceLength) + maskValue.substring((parms.offset - 2) * 4 + replaceLength);
    						//maskValue = maskValue.replaceFirst(maskValue.substring((parms.offset - 2) * 4, (parms.offset - 2) * 4 + replaceLength), inputValue.substring(0, replaceLength));
    						
    						// 更新本窗口的标签EPC
        					txtEpc.setText(maskValue);
        					
        					// 更新读取窗口的标签EPC
        					final FragmentManager fragmentManager = getActivity()
        							.getFragmentManager();
        					TagReadFragment objFragment = (TagReadFragment) fragmentManager
        							.findFragmentById(R.id.fragment_tagRead);
        					TextView txtReadEpc = (TextView) objFragment.getActivity().findViewById(
        							R.id.txtReadEpc);
        					txtReadEpc.setText(maskValue);
        					
        					// 更新标签列表窗口中的标签EPC
        					HKUHFActivity.objFragment.updateSelItem(maskValue);
    					}
    				}
    				
    				Toast.makeText(getActivity(),
                			getText(R.string.info_write_success),
    						Toast.LENGTH_SHORT).show();
    			}
    			else {
    				txtWarnning.setText(getText(R.string.info_tag_write_error));
				}
    		}else {
    			txtWarnning.setText(getText(R.string.info_no_tags));
			}
    	}
    	catch (radioBusyException e) {
    		Toast.makeText(getActivity(),
    				getText(R.string.info_readio_buzy),
					Toast.LENGTH_SHORT).show();
		}
    	catch (radioFailException e) {
    		Toast.makeText(getActivity(),
    				getText(R.string.info_oper_fail),
					Toast.LENGTH_SHORT).show();
    	}
    	catch (Exception e) {
    		Toast.makeText(getActivity(),
    				getText(R.string.info_oper_fail),
					Toast.LENGTH_SHORT).show();
    	}
    	
    	// 无论是否发生异常，都进行一次取消选择操作
    	try {
    		SingulationCriteria dissingulationCriteria = new SingulationCriteria();
    		dissingulationCriteria.status = SingulationCriteriaStatus.Disabled;
    		dissingulationCriteria.offset = 0;
    		dissingulationCriteria.count = 0;
    		dissingulationCriteria.match = matchType.Regular;
            
            ctrlOperateResult result = ((HKUHFActivity)getActivity()).myRadio.Set18K6CPostMatchCriteria(dissingulationCriteria);
            if (result != ctrlOperateResult.OK)
            {
            	Toast.makeText(getActivity(),
            			result.toString(),
						Toast.LENGTH_SHORT).show();
            }
		} catch (radioBusyException e) {
			Toast.makeText(getActivity(),
    				getText(R.string.info_readio_buzy),
					Toast.LENGTH_SHORT).show();
		}
    }
    
    public void write(){
		String ap = editAccesspwd.getText().toString();
		short epcLength = (short) (txtEpc.getText().toString().length() / 2);
		String epc = txtEpc.getText().toString();
		byte mb = (byte) spinnerArea.getSelectedItemPosition();
		switch (mb) {
		case 0:
			mb++;
			break;
		case 1:
			mb += 2;
			break;
		default:
			break;
		}
		short sa = Short.parseShort(unmpOffset.getSelectedItem().toString());
		short dl = Short.parseShort(unmpLength.getSelectedItem().toString());
		String writeData = editInput.getText().toString();
		if(!TextUtils.isEmpty(writeData)&& writeData.length()/4==dl){
			byte[] arguments = Bytes.concat(DataUtils.hexStringTobyte(ap),
					DataUtils.short2byte(epcLength),
					DataUtils.hexStringTobyte(epc), new byte[] { mb },
					DataUtils.short2byte(sa), DataUtils.short2byte(dl),DataUtils.hexStringTobyte(writeData));
			String data = writeTag(arguments);
			if(!TextUtils.isEmpty(writeData) && data.equals("00")){
				ToastUtil.showToast(getActivity(), "写入成功！");
				writeFlag = false;
			}
		}

    }
    
	public String writeTag(byte[] args){
		Response response = ((UHFActivity)getActivity()).api.writeTypeCTagData(args);
		if(response.result == Response.RESPONSE_PACKET && response.data != null){
			return DataUtils.toHexString(response.data);
		}
		return "";
	}
}
