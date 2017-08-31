package com.android.zhhr.presenter;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.zhhr.R;
import com.android.zhhr.data.entity.Comic;
import com.android.zhhr.module.ComicModule;
import com.android.zhhr.ui.custom.IndexItemView;
import com.android.zhhr.ui.view.IDetailView;
import com.android.zhhr.utils.ShowErrorTextUtil;

import rx.Subscriber;

/**
 * Created by 皓然 on 2017/7/12.
 */

public class ComicDetailPresenter extends  BasePresenter<IDetailView>{
    private Context context;
    private long mComicId;
    private boolean isOrder;

    public Comic getmComic() {
        return mComic;
    }

    public void setmComic(Comic mComic) {
        this.mComic = mComic;
    }

    public boolean isOrder() {
        return isOrder;
    }

    public void setOrder(boolean order) {
        isOrder = order;
    }

    private Comic mComic;
    private ComicModule mModel;
    public ComicDetailPresenter(Activity context, IDetailView view) {
        super(context, view);
        this.context = context;
        this.mComic = new Comic();
        this.mModel = new ComicModule(context);
    }

    /**
     * 加载详情
     * @param comic_id
     */
    public void getDetail(String comic_id){
        if(comic_id==null){
            mView.ShowToast("获取ID失败");
        }else{
            mComicId =  Long.parseLong(comic_id);
            mModel.getComicDetail(comic_id,new Subscriber<Comic>() {
                @Override
                public void onCompleted() {
                    mView.getDataFinish();
                }

                @Override
                public void onError(Throwable throwable) {
                    mView.showErrorView(ShowErrorTextUtil.ShowErrorText(throwable));
                }

                @Override
                public void onNext(Comic comic) {
                    comic.setId(mComicId);
                    mComic = comic;
                    SaveComicToDB(mComic);
                    mView.fillData(comic);
                    mView.ShowToast("之前看到"+(mComic.getCurrentChapter())+"话");
                }
            });
            mModel.isCollected(mComicId, new Subscriber<Boolean>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Boolean isCollect) {
                    if(!isCollect){
                        mView.setCollect();
                    }
                }
            });
        }
    }

    public void getCurrentChapters(){
        mModel.getComicFromDB(mComicId, new Subscriber<Comic>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mView.ShowToast("获取当前章节数目失败"+e.toString());
            }

            @Override
            public void onNext(Comic mComic) {
                if(mComic!=null){
                    mView.setCurrent(mComic.getCurrentChapter());
                }
            }
        });
    }

    public void collectComic(){
        final java.util.Date date = new java.util.Date();
        long datetime = date.getTime();
        mComic.setCrateTime(datetime);
        mComic.setIsCollected(true);
        mModel.updateComicToDB(mComic, new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mView.ShowToast("已经收藏");
            }

            @Override
            public void onNext(Boolean CanSelect) {
                if(CanSelect){
                    mView.setCollect();
                    mView.ShowToast("收藏成功"+date.toString());
                }else{
                    mView.ShowToast("收藏失败");
                }
            }
        });
    }

    public void SaveComicToDB(Comic mComic){
        mModel.saveComicToDB(mComic, new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mView.ShowToast("保存到数据库失败"+e.toString());
            }

            @Override
            public void onNext(Boolean CanSelect) {
                if(CanSelect){
                    mView.ShowToast("保存到数据库成功");
                }else{
                    mView.ShowToast("收藏失败");
                }
            }

        });
    }


    public void orderIndex(LinearLayout mlayout) {
        for(int position=0;position<mComic.getChapters().size();position++){
            IndexItemView itemView = (IndexItemView) mlayout.getChildAt(position);
            TextView textView = (TextView) itemView.getChildAt(0);
            if(!isOrder()){
                textView.setText((position+1)+" - "+mComic.getChapters().get(position));
            }else{
                textView.setText((mComic.getChapters().size()-position)+" - "+mComic.getChapters().get(mComic.getChapters().size()-1-position));
            }
        }
        if(!isOrder()){
            mView.OrderData(R.mipmap.zhengxu);
        }else{
            mView.OrderData(R.mipmap.daoxu);
        }
    }
}
