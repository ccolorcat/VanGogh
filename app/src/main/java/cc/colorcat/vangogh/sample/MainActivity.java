package cc.colorcat.vangogh.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import cc.colorcat.vangogh.VanGogh;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE = 0x89;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.iv_local);
        findViewById(R.id.btn_pick_drawable).setOnClickListener(mClick);
        findViewById(R.id.btn_pick_image).setOnClickListener(mClick);
        findViewById(R.id.btn_vangogh).setOnClickListener(mClick);
    }

    private View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_vangogh:
                    startActivity(new Intent(MainActivity.this, VanGoghActivity.class));
                    break;
                case R.id.btn_pick_image:
                    pickImage();
                    break;
                case R.id.btn_pick_drawable:
                    VanGogh.get().load(R.mipmap.ic_launcher).into(mImageView);
                    break;
                default:
                    break;
            }
        }
    };

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE) {
            VanGogh.get().load(data.getData()).into(mImageView);
        }
    }
}
