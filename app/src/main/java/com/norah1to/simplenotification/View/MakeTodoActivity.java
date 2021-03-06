package com.norah1to.simplenotification.View;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.norah1to.simplenotification.Entity.Tag;
import com.norah1to.simplenotification.Entity.Todo;
import com.norah1to.simplenotification.Notification.Action;
import com.norah1to.simplenotification.Notification.ActionCreateImpl;
import com.norah1to.simplenotification.Notification.ActionMakeAlarm;
import com.norah1to.simplenotification.Notification.ActionMakeNotification;
import com.norah1to.simplenotification.Notification.Notification;
import com.norah1to.simplenotification.R;
import com.norah1to.simplenotification.Settings.SharePreferencesHelper;
import com.norah1to.simplenotification.Util.ChipUtil;
import com.norah1to.simplenotification.Util.DateUtil;
import com.norah1to.simplenotification.ViewModel.MakeTodoViewModel;
import com.norah1to.simplenotification.ViewModel.TagViewModel;
import com.norah1to.simplenotification.ViewModel.TodoViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MakeTodoActivity extends BaseActivity {

    private static final String TAG = "MakeTodoActivity";

    private MakeTodoViewModel makeTodoViewModel;
    private TodoViewModel todoViewModel;
    private TagViewModel tagViewModel;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    private TextInputEditText contentInput;

    private FloatingActionButton fab;

    private MaterialTextView dateTextView;

    private BottomAppBar bottomAppBar;

    private ChipGroup tagGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_todo);


        // 初始化 textinput
        contentInput = (TextInputEditText)findViewById(R.id.text_maketodo_content);


        // 初始化 fab
        fab = (FloatingActionButton)findViewById(R.id.fab_maketodo);


        // 初始化日期显示
        dateTextView = (MaterialTextView)findViewById(R.id.text_maketodo_date);


        // 初始化底栏
        bottomAppBar = (BottomAppBar)findViewById(R.id.bottom_bar_maketodo);


        // 初始化 tagGroup
        tagGroup = (ChipGroup)findViewById(R.id.chip_group_maketodo_tagGroup);


        // 初始化自己的 viewModel
        makeTodoViewModel = ViewModelProviders.of(this).get(MakeTodoViewModel.class);
        // 初始化 mTags
        new Thread(() -> {
            makeTodoViewModel.setmTags();
        }).start();
        // 监听 mData 变化
        makeTodoViewModel.getmData().observe(this, mData -> {
            // 更改显示的内容
            dateTextView.setText(new StringBuilder().
                    append(getResources().getString(R.string.prefix_text_maketodo_date)).
                    append(DateUtil.formDatestr(mData)).toString());
        });
        // 监听添加的 tags 变化
        makeTodoViewModel.getmAddTags().observe(this, mAddTags -> {
            tagGroup.removeAllViews();
            for (Tag tag : mAddTags) {
                tagGroup.addView(ChipUtil.createChip(this, tag.getName(), getMenuCloseListener()));
            }
        });
        // 监听 mTodo 的变化
        makeTodoViewModel.getmTodo().observe(this, mTodo -> {
            // 显示更改的内容
            String tmpInputStr = contentInput.getText().toString();
            if (tmpInputStr.equals("")) {
                contentInput.setText(mTodo.getContent());
                contentInput.selectAll();
            }
            // 更改通知图标
            ActionMenuItemView menuItemNotice =
                    (ActionMenuItemView) findViewById(R.id.menuitem_maketodo_notice);
            if (mTodo.getNotice() == Todo.STATE_NOTICE) {
                menuItemNotice.setIcon(ContextCompat.
                        getDrawable(this, R.drawable.ic_notifications_black_24dp));
            } else {
                menuItemNotice.setIcon(ContextCompat.
                        getDrawable(this, R.drawable.ic_notifications_off_grey_24dp));
            }
            // 修改优先级图标
            ActionMenuItemView menuItemPriority =
                    (ActionMenuItemView) findViewById(R.id.menuitem_maketodo_priority);
            switch (mTodo.getPriority()) {
                case Todo.PROIORITY_HIGH:
                    menuItemPriority.setIcon(ContextCompat.
                            getDrawable(this, R.drawable.ic_priority_pink_24dp));
                    break;
                case Todo.PROIORITY_MID:
                    menuItemPriority.setIcon(ContextCompat.
                            getDrawable(this, R.drawable.ic_priority_orange_24dp));
                    break;
                case Todo.PROIORITY_LOW:
                    menuItemPriority.setIcon(ContextCompat.
                            getDrawable(this, R.drawable.ic_priority_gray_24dp));
                    break;
                default:
                    menuItemPriority.setIcon(ContextCompat.
                            getDrawable(this, R.drawable.ic_priority_gray_24dp));
                    break;
            }
        });


        // 获得 MainActivity 中的 viewModel
        if (MainActivity.mtodoViewModel != null) {
            todoViewModel = MainActivity.mtodoViewModel;
        } else {
            todoViewModel = new TodoViewModel(getApplication());
        }


        // 获得 TagActivity 中的 viewModel
        if (TagActivity.tagViewModel != null) {
            tagViewModel = TagActivity.tagViewModel;
        } else {
            tagViewModel = new TagViewModel(getApplication());
        }


        // 如果是修改原有的，则根据 id 初始化内容
        Intent todoData = getIntent();
        if (todoData != null && todoData.getSerializableExtra(Todo.TAG) != null) {
            String todoID = ((Todo)todoData.getSerializableExtra(Todo.TAG)).getTodoID();
            new Thread(() -> {
                makeTodoViewModel.setmTodo(todoID);
            }).start();
        } else {
            makeTodoViewModel.getmTodo().setValue(new Todo());
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onStart() {
        super.onStart();


        // 初始化 date，默认为明天
        if (makeTodoViewModel.getmData().getValue() == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            if (c.get(Calendar.HOUR_OF_DAY) >
                    SharePreferencesHelper.getSecondDayStartHour(this)) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            c.set(Calendar.HOUR_OF_DAY, SharePreferencesHelper.getNoticeDateHour(this));
            c.set(Calendar.MINUTE, SharePreferencesHelper.getNoticeDateMin(this));
            makeTodoViewModel.getmData().setValue(c.getTime());
        }


        // 输入框获取焦点
        contentInput.requestFocus();


        // 监听 fab 点击
        fab.setOnClickListener(v -> {
            makeTodo(false);
        });
        fab.setOnLongClickListener(v -> {
            makeTodo(true);
            return true;
        });


        // 监听 bottomBar 菜单点击
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuitem_maketodo_tag:
                    showTagPopmenu(bottomAppBar);
                    return true;
                case R.id.menuitem_maketodo_setdate:
                    showDatePickerDialog();
                    return true;
                case R.id.menuitem_maketodo_notice:
                    changeNoticeState();
                    return true;
                case R.id.menuitem_maketodo_settime:
                    showTimePickerDialog();
                    return true;
                case R.id.menuitem_maketodo_priority:
                    changePriority();
                    return true;
                default:
                    return false;
            }
        });
    }


    // 创建或者更新一个 todoObj
    private void makeTodo(boolean notice) {
        Todo todo = makeTodoViewModel.getmTodo().getValue();
        if (todo == null) {
            todo = new Todo();
        }
        if (todo.getNoticeCode() == Todo.CODE_NULL) {
            todo.setNoticeCode(100000 + new Random().nextInt(899999));
        }
        Log.d(TAG, "makeTodo: code: " + todo.getNoticeCode());
        todo.setContent(contentInput.getText().toString());
        todo.setNoticeTimeStamp(makeTodoViewModel.getmData().getValue());
        todo.setModifiedTimeStamp(new Date());
        todo.setCompletedTimeStamp(null);
        todo.setTags(new ArrayList<Tag>(makeTodoViewModel.getmAddTags().getValue()));
        if (todo.getCreatedTimeStamp() == null)
            todo.setCreatedTimeStamp(new Date());
        todoViewModel.insertTodo(todo);
        // Todo: 推送通知 & 创建提醒
        Notification notification = new Notification(todo);
        Action action = new ActionCreateImpl();
        action = new ActionMakeAlarm(action);
        if (notice) {
            action = new ActionMakeNotification(action);
        }
        notification.setMyAction(action);
        notification.doAction(MakeTodoActivity.this);
        finish();
    }


    // 显示标签选择 popMenu
    private void showTagPopmenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        List<Tag> tagList = makeTodoViewModel.getmTags().getValue();
        // 为空的时候不处理
        if (tagList == null || tagList.size() == 0) {
            Toast.makeText(this,
                    this.getResources().getString(R.string.toast_make_todo_no_tags),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        for (Tag tag : tagList) {
            menu.add(tag.getName());
        }
        // 监听点击
        popup.setOnMenuItemClickListener(item -> {
            tagGroup.addView(ChipUtil.createChip(this, item.getTitle().toString(), getMenuCloseListener()));
            Set<Tag> tmpSet = makeTodoViewModel.getmAddTags().getValue();
            Tag tmpTag = new Tag();
            tmpTag.setName(item.getTitle().toString());
            tmpSet.add(tmpTag);
            makeTodoViewModel.getmAddTags().setValue(tmpSet);
            return true;
        });
        popup.show();
    }

    private View.OnClickListener getMenuCloseListener() {
        return v -> {
            Tag delTag = new Tag();
            delTag.setName(((Chip)v).getText().toString());
            try {
                Set<Tag> tmpSet = makeTodoViewModel.getmAddTags().getValue();
                tmpSet.remove(delTag);
                Log.d(TAG, "getMenuCloseListener: " + tmpSet.size());
                makeTodoViewModel.getmAddTags().postValue(tmpSet);
            } catch (Exception e) {
                Log.e(TAG, "showTagPopmenu: ", e);
            }
        };
    }


    // 显示一个日期选择器
    private void showDatePickerDialog() {
        Date tmpDate = makeTodoViewModel.getmData().getValue();
        datePickerDialog = new DatePickerDialog(this, ((view, year, month, dayOfMonth) -> {
            tmpDate.setYear(year - 1900);
            tmpDate.setMonth(month);
            tmpDate.setDate(dayOfMonth);
            makeTodoViewModel.getmData().setValue(tmpDate);
        }), tmpDate.getYear() + 1900, tmpDate.getMonth(), tmpDate.getDate());
        datePickerDialog.show();
    }


    // 显示一个时间选择器
    private void showTimePickerDialog() {
        Date tmpDate = makeTodoViewModel.getmData().getValue();
        timePickerDialog = new TimePickerDialog(this, ((view, hourOfDay, minute) -> {
            tmpDate.setHours(hourOfDay);
            tmpDate.setMinutes(minute);
            makeTodoViewModel.getmData().setValue(tmpDate);
        }), tmpDate.getHours(), tmpDate.getMinutes(), false);
        timePickerDialog.show();
    }


    // 添加、移除提醒
    private void changeNoticeState() {
        Todo tmpTodo = makeTodoViewModel.getmTodo().getValue();
        tmpTodo.setNotice(tmpTodo.getNotice() == Todo.STATE_NOTICE ?
                Todo.STATE_NOT_NOTICE : Todo.STATE_NOTICE);
        makeTodoViewModel.getmTodo().setValue(tmpTodo);
    }

    // 切换优先级
    private void changePriority() {
        Todo tmpTodo = makeTodoViewModel.getmTodo().getValue();
        int selectedIndex = 3 - tmpTodo.getPriority() / 50;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        CharSequence[] characters = {"高", "中", "低"};
        builder.setSingleChoiceItems(characters, selectedIndex, ((dialog, which) -> {
            tmpTodo.setPriority((3 - which) * 50);
            makeTodoViewModel.getmTodo().setValue(tmpTodo);
            dialog.dismiss();
        }));
        builder.create().show();
    }
}
