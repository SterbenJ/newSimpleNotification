package com.norah1to.simplenotification.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BlurMaskFilter;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;
import com.norah1to.simplenotification.Entity.Tag;
import com.norah1to.simplenotification.Entity.Todo;
import com.norah1to.simplenotification.MainActivity;
import com.norah1to.simplenotification.MakeTodoActivity;
import com.norah1to.simplenotification.R;
import com.norah1to.simplenotification.Util.DateUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoViewHolder> {

    private static final String TAG = "TodoListAdapter";

    public static final int STATE_ACTION_MODE_ON = 1;
    public static final int STATE_ACTION_MODE_OFF = 0;

    // 代理
    private Proxy mProxy;


    // 模式
    public int actionModeState = STATE_ACTION_MODE_OFF;


    // 被选中的列表
    private Set<Integer> selectIndexs = new HashSet<Integer>() {
    };


    class TodoViewHolder extends  RecyclerView.ViewHolder {
        // 输入的内容
        private final MaterialTextView contentView;
        // 日期显示
        private final MaterialTextView dateView;
        // 根布局（卡片）
        private final MaterialCardView cardView;
        // 提醒图标
        private final ImageView alarmImgView;
        // 优先级强调色
        private final View priorityColorView;
        // 标签显示
        private final MaterialTextView tagsView;
        // actionMode 选中按钮
        private final MaterialRadioButton radioButton;
        // checkbox 完成按钮
        private final MaterialCheckBox checkBox;

        private TodoViewHolder (View itemView) {
            super(itemView);
            contentView = itemView.findViewById(R.id.text_todoitem_content);
            dateView = itemView.findViewById(R.id.text_todoitem_date);
            cardView = itemView.findViewById(R.id.card_todoitem);
            alarmImgView = itemView.findViewById(R.id.img_todoitem);
            priorityColorView = itemView.findViewById(R.id.color_view_todoitem);
            tagsView = itemView.findViewById(R.id.text_todoitem_tags);
            radioButton = itemView.findViewById(R.id.radio_todoitem);
            checkBox = itemView.findViewById(R.id.checkbox_todoitem);
        }
    }


    private final LayoutInflater mInflater;


    // 数据列表
    private List<Todo> mTodos;


    public TodoListAdapter(Context context) {
        // 加载布局
        mInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.todo_item, parent, false);
        return new TodoViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        if (mTodos != null) {
            Todo current = mTodos.get(position);

            // 根据 actionMode 修改按钮
            switch (actionModeState) {
                case STATE_ACTION_MODE_OFF:
                    // 隐藏 radiobtn
                    holder.radioButton.setVisibility(View.GONE);
                    holder.radioButton.setChecked(false);
                    // 显示 checkbox
                    holder.checkBox.setVisibility(View.VISIBLE);
                    break;
                case STATE_ACTION_MODE_ON:
                    // 显示 radiobtn
                    holder.radioButton.setVisibility(View.VISIBLE);
                    // 隐藏 checkbox
                    holder.checkBox.setVisibility(View.GONE);
                    break;
            }

            // 设置点击监听
            holder.itemView.setOnClickListener(v -> {
                switch (actionModeState) {
                    case STATE_ACTION_MODE_OFF:
                        // 点击跳转到编辑页
                        Log.d(TAG, "onBindViewHolder: " +
                                "position: " + position +
                                "todo: " + "\n" + current.toString());
                        Intent intent = new Intent(holder.itemView.getContext(), MakeTodoActivity.class);
                        // Intent 中传入 TodoID
                        intent.putExtra(Todo.TAG, current.getTodoID());
                        holder.itemView.getContext().startActivity(intent);
                        break;
                    case STATE_ACTION_MODE_ON:
                        // TODO:
                        if (mTodos.get(position).isChecked()) {
                            mTodos.get(position).setChecked(false);
                            holder.radioButton.setChecked(false);
                            selectIndexs.remove(position);
                        } else {
                            holder.radioButton.setChecked(true);
                            mTodos.get(position).setChecked(true);
                            selectIndexs.add(position);
                        }
                        break;
                }
            });

            holder.contentView.setText(current.getContent());
            holder.dateView.setText(DateUtil.formDatestr(current.getNoticeTimeStamp()));

            // 根据优先级切换卡片强调色
            switch (current.getPriority()) {
                case Todo.PROIORITY_HIGH: // 高优先级
                    holder.priorityColorView.setBackgroundColor(
                            ContextCompat.getColor(holder.itemView.getContext(),
                                    R.color.colorPriorityPink));
                    break;
                case Todo.PROIORITY_MID: // 中优先级
                    holder.priorityColorView.setBackgroundColor(
                            ContextCompat.getColor(holder.itemView.getContext(),
                                    R.color.colorPriorityOrange));
                    break;
                case Todo.PROIORITY_LOW: // 低优先级
                    holder.priorityColorView.setBackgroundColor(
                            ContextCompat.getColor(holder.itemView.getContext(),
                                    R.color.colorPriorityGrey));
                    break;
                default: // 默认低优先级样式
                    holder.priorityColorView.setBackgroundColor(
                            ContextCompat.getColor(holder.itemView.getContext(),
                                    R.color.colorPriorityGrey));
                    break;
            }



            // 设置提醒图标是否展示
            if (current.getNotice() == Todo.STATE_NOT_NOTICE) {
                holder.alarmImgView.setVisibility(View.GONE);
            } else {
                holder.alarmImgView.setVisibility(View.VISIBLE);
            }

            // 设置 tags 显示
            SpannableStringBuilder spannableString = new SpannableStringBuilder();
            int endIndex = 0;
            for (Tag tag : current.getTags()) {
                String text = tag.getName();
                spannableString.append(' ');
                spannableString.append(text);
                spannableString.append(' ');

                // 设置背景色
                BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(
                        ContextCompat.getColor(holder.itemView.getContext(),
                                R.color.color_secondary)
                );
                spannableString.setSpan(
                        backgroundColorSpan,
                        endIndex,
                        endIndex + text.length() + 2,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // 设置文字颜色
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                        ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black)
                );
                spannableString.setSpan(
                        colorSpan,
                        endIndex,
                        endIndex + text.length() + 2,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // 设置斜体
                StyleSpan styleSpan = new StyleSpan(Typeface.ITALIC);
                spannableString.setSpan(
                        styleSpan,
                        endIndex,
                        endIndex + text.length() + 2,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                spannableString.append(" ");
                endIndex += text.length() + 3;
            }
            holder.tagsView.setText(spannableString);

            // 长按开启 actionMode
            holder.itemView.setOnLongClickListener(v -> {
                mProxy.mStartActionMode(holder.itemView);
                return true;
            });
        } else {
            holder.contentView.setText("No todo");
            holder.dateView.setText("No date");
        }
    }


    // 设置数据列表
    public void setTodos(List<Todo> todos) {
        mTodos = todos;
        notifyDataSetChanged();
    }


    // 往列表中添加一个并且局部刷新（带动画
    public void addTodo(Todo todo) {
        if (mTodos != null) {
            // 加到顶部
            mTodos.add(0, todo);
            // 适配器中插入顶部
            notifyItemInserted(0);
            Log.d(TAG, "addTodo: itemCount" + getItemCount());
            // 适配器刷新顶部
            notifyItemRangeChanged(0, 1);
        }
    }


    // 设置代理
    public void setmProxy(Proxy proxy) {
        this.mProxy = proxy;
    }


    @Override
    public int getItemCount() {
        if (mTodos != null)
            return mTodos.size();
        else return 0;
    }


    // 删除所选项目
    public void deleteSeletedItems() {
        for (Integer integer : this.selectIndexs) {
            Log.d(TAG, "deleteSeletedItems: index" + integer.intValue());
            Log.d(TAG, "deleteSeletedItems: mTodossize: " + mTodos.size());
            new Thread(() -> {
                Todo tmpTodo = mTodos.get(integer.intValue());
                MainActivity.mtodoViewModel.delete(tmpTodo.getTodoID(), Todo.STATE_DELETED);
            }).start();
        }
        selectIndexs.clear();
    }


    // 设置 actionMode 模式
    public void setActionModeState(int actionModeState) {
        this.actionModeState = actionModeState;
        this.selectIndexs.clear();
        this.notifyDataSetChanged();
    }

    // 给外界实现的代理接口
    public interface Proxy {
        // 长按卡片的时候会被调用，开启 actionMode
        void mStartActionMode(View view);
    }
}
