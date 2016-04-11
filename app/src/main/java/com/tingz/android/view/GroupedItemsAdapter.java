package com.tingz.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tingz.android.R;
import com.tingz.android.view.helper.ListItemModel;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by steinerro on 3/02/2016.
 */
public abstract class GroupedItemsAdapter<T extends ListItemModel> extends RecyclerView.Adapter<ListItemViewHolder> {


    private static final int KEY_DECORATED = R.id.recycler_view_grouped_items_decoration;

    private static final int KEY_LAST_ITEM = R.id.recycler_view_last_item;

    private static final Object YES = new Object();

    private static final Object NO = new Object();

    /**
     * the view types this adapter uses - shifted 0x1 frees the spectrum of [1,2^7) for derived classes to use
     */
    private static final int VIEW_TYPE_HEADER = 0x1 << 7;

    private static final int VIEW_TYPE_FOOTER = 0x1 << 8;

    private static final int VIEW_TYPE_LABEL = 0x1 << 9;

    private static final int VIEW_TYPE_FIRST_ROW = 0x1 << 10;

    private static final int VIEW_TYPE_LAST_ROW = 0x1 << 11;

    private static final int VIEW_TYPE_MID_ROW = 0x1 << 12;

    private static final int VIEW_TYPE_MASK = VIEW_TYPE_HEADER | VIEW_TYPE_LABEL | VIEW_TYPE_FIRST_ROW | VIEW_TYPE_MID_ROW | VIEW_TYPE_LAST_ROW | VIEW_TYPE_FOOTER;

    /**
     * quickly get the layout id for a parent/wrapped of a view by it's type.
     * also get hte view group id to which the wrapped view need to be added.
     *
     * @see #onCreateViewHolder
     * @see #getResourceForWrappedType
     */
    private final static SparseIntArray wrapperViewIdForType = new SparseIntArray();

    private final static SparseIntArray wrapperParentIdForType = new SparseIntArray();

    static {
        wrapperViewIdForType.append(VIEW_TYPE_FIRST_ROW, R.layout.list_item_recycler_group_first_row); // the first row in a group - wrapped to create an illusion of elevation
        wrapperViewIdForType.append(VIEW_TYPE_LAST_ROW, R.layout.list_item_recycler_group_last_row); // the last row in a group - wrapped to create an illusion of elevation
        wrapperViewIdForType.append(VIEW_TYPE_FOOTER, R.layout.list_item_recycler_footer); // the last row in the adapter - wrapped to create an illusion of elevation and bottom padding

        wrapperParentIdForType.append(VIEW_TYPE_FIRST_ROW, R.id.recycler_view_first_row);
        wrapperParentIdForType.append(VIEW_TYPE_LAST_ROW, R.id.recycler_view_last_row);
        wrapperParentIdForType.append(VIEW_TYPE_FOOTER, R.id.recycler_view_last_row);
    }

    // map start-positions of the displayed groups represented through GroupInfoWrapper objects
    private SparseArray<GroupInfoWrapper> displayedGroups;

    // since the majority of items are expected to be displayable values of type <T extends ListItemModelWrapper> it's easily accessed during @code #onBindViewHolder
    private ArrayList<Object> mappedItems;

    private int totalViews = 0;

    private ArrayList<GroupInfo> groupsInfo;

    public GroupedItemsAdapter() {
        super();
        //be the first to know and organize things :)
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                buildGroupedItemsData();

            }

            @Override
            public void onItemRangeRemoved(final int positionStart, final int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                Log.v("RHF", String.format("removing at %d, %d items", positionStart, itemCount));
                onChanged();
            }

            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                Log.v("RHF", String.format("inserting at %d, %d items", positionStart, itemCount));
                onChanged();
            }
        });
    }

    public void setGroupsInfo(List<? extends GroupInfo> groups) {
        this.groupsInfo = new ArrayList<>(groups);
        notifyDataSetChanged();
    }

    /**
     * whether or not this adapter displays a header. true if {@code #getHeaderLayoutId} returns a valid layout resource id
     * to setup the header
     *
     * @return yes or no for header
     * @see #setupHeader
     */
    protected final boolean hasHeader() {
        return getHeaderLayoutId() != -1;
    }

    /**
     * whether or not to let hte header display hte first group's label.
     * if so, hte header is expected to include a view with id/list_item
     *
     * @return true if the header displays the first group's label, which is usually the case.
     */
    protected boolean headerDisplaysFirstLabel() {
        return hasHeader();
    }

    protected int getHeaderLayoutId() {
        return -1;
    }

    protected int getListItemLayoutId(final int viewType) {
        return getListItemLayoutId();
    }

    protected int getListItemLayoutId() {
        return R.layout.list_item_with_icon;
    }

    protected int getListGroupSectionHeaderLayoutId() {
        return R.layout.list_section_item;
    }

    /**
     * override this method to setup the header during {@link #onBindViewHolder}
     *
     * @param header - teh header view inflated by @code #onCreateViewHolder using the id provided by {@link #getHeaderLayoutId}
     */
    protected void setupHeader(View header) {

    }

    /**
     * returns a layout resource id for the view type. this layout will be wrapped in another view
     * if it is placed as a first/last row in a group, or is the last items in the recycler.
     * if the layout uses the common id {@code R.id.list_item} and implements {@linkplain LabelItemView} (or is a TextView)
     * then the view holder will easily handle it during #onBindViewHolder
     *
     * @param viewType
     * @return
     */
    protected int getResourceForType(final int viewType) {
        int id;
        if (isArchViewType(viewType, VIEW_TYPE_HEADER) && hasHeader()) {
            id = getHeaderLayoutId();
        } else {
            id = isArchViewType(viewType, VIEW_TYPE_LABEL) ?
                    getListGroupSectionHeaderLayoutId() :
                    getListItemLayoutId((~VIEW_TYPE_MASK) & viewType);
        }
        return id;
    }

    private boolean isArchViewType(final int viewType, final int archViewType) {
        return (viewType & archViewType) != 0;
    }

    private int getResourceForWrappedType(final int viewType) {

        return wrapperViewIdForType.get(viewType & VIEW_TYPE_MASK, -1);
    }

    private int getParentIdForWrappedType(final int viewType) {

        return wrapperParentIdForType.get(viewType & VIEW_TYPE_MASK, -1);
    }

    /**
     * usually the header, label, last row and footer are decorated (wrapped) during #onCreateViewHolder
     */
    public boolean isItemDecorated(View view) {
        return view != null && view.getTag(KEY_DECORATED) == YES;
    }

    public boolean isLastItem(View view) {
        return view != null && view.getTag(KEY_LAST_ITEM) == YES;
    }

    /**
     * since this adapter wraps view to create teh illusion of groups and card+elevations is is hard(er) to attach {@link android.view.View.OnClickListener}
     * to the desired view. it is also harder to map it to the corresponding item on display.
     * <p/>
     * this method should help.
     *
     * @param item     - the item displayed by this view
     * @param position - the position of hte item in this adapter
     * @param v        - the view that was clicked (not the wrapper of it)
     */

    abstract protected void onItemClicked(final T item, final int position, final View v);

    /**
     * get the group info for a given position where a label is supposed to be
     *
     * @param position a position in the adapter, ranging from 0 to @code #getItemCount
     * @return the group if the position marks the beginning of a group, null otherwise
     */
    protected GroupInfoWrapper getGroupInfoAtPosition(final int position) {
        return displayedGroups.get(position, null);
    }

    /**
     * get the item at the adapter position. return null the position doesn't specify a ListItemModel (e.g a label/header)
     *
     * @param adapterPosition
     * @return
     */
    public T get(final int adapterPosition) {
        T item = null;
        try {
            item = (T) mappedItems.get(adapterPosition);
        } catch (Exception e) {
        }
        return item;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(getResourceForType(viewType), parent, false);
        if (isArchViewType(viewType, VIEW_TYPE_HEADER)) {
            setupHeader(view);
        }

        int wrapperViewId = getResourceForWrappedType(viewType);
        if (wrapperViewId > 0) {
            ViewGroup wrapper = (ViewGroup) layoutInflater.inflate(wrapperViewId, parent, false);
            int parentId = getParentIdForWrappedType(viewType);
            ViewGroup viewGroup = (ViewGroup) wrapper.findViewById(parentId);
            viewGroup.addView(view, viewGroup.getChildCount());
            view = wrapper;
        }
        view.setTag(KEY_DECORATED, shouldDecorateItem(viewType) ?
                NO :
                YES);
        view.setTag(KEY_LAST_ITEM, isArchViewType(viewType, VIEW_TYPE_FOOTER) ?
                YES :
                NO);
        return createListItemViewHolder(view, (~VIEW_TYPE_MASK) & viewType);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, final int position) {

        final T item = get(position);
        if (item != null) {
            onBindGroupItem(holder, position, item);
        } else if (position != 0 || !hasHeader() || headerDisplaysFirstLabel()) { // it's a label
            GroupInfoWrapper groupInfoAtPosition = getGroupInfoAtPosition(position);
            onBindGroupInfo(holder, groupInfoAtPosition != null ?
                    groupInfoAtPosition.getGroupInfo() :
                    null);
        }
    }

    /**
     * used for binding values to the group's header. usually just a label, sometimes more than that
     *
     * @param holder
     * @param group
     */
    protected void onBindGroupInfo(final ListItemViewHolder holder, final GroupInfo group) {
        CharSequence label = group != null ?
                group.getLabel() :
                null;
        holder.listItemView.setPrimaryText(label);
        /*
        View view = holder.listItemView.asView();
        TypedArray typedArray = view.getContext().obtainStyledAttributes(R.style.DefaultListSectionItem, R.styleable.Theme);
        int dimensionPixelSize = typedArray.getDimensionPixelSize(R.styleable.Theme_listPreferredItemHeightSmall, 100);

        view.getLayoutParams().height = label != null ?
                dimensionPixelSize :
                dimensionPixelSize / 3;
        typedArray.recycle();*/
    }

    /**
     * used to bind values to the items in each group
     *
     * @param holder
     * @param position
     * @param item
     */
    protected void onBindGroupItem(final ListItemViewHolder holder, final int position, final T item) {
        holder.iconItemView.setIcon(item.getIconRes());

        holder.listItemView.setPrimaryText(item.getPrimaryText());
        holder.listItemView.setSecondaryText(item.getSecondaryText());
        holder.listItemView.setExtraText(item.getExtraText());

        View v = holder.listItemView.asView();
        if (v!=null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onItemClicked(item, position, v);
                }
            });
        }
    }

    @Override
    public int getItemCount() {

        return totalViews;
    }

    /**
     * marking this method final to have a control of the wrappeer views
     * if you wish to specify different view type override #getItemViewTypeInternal
     *
     * @param position
     * @return
     */
    @Override
    public final int getItemViewType(final int position) {
        int type = getItemViewTypeInternal(position);
        assert type < VIEW_TYPE_HEADER;

        if (position == 0 && hasHeader()) {
            type |= VIEW_TYPE_HEADER;
        } else if (position == getItemCount() - 1) {
            type |= VIEW_TYPE_FOOTER;
        } else if (getGroupInfoAtPosition(position) != null) {
            type |= VIEW_TYPE_LABEL;
        } else if (getGroupInfoAtPosition(position + 1) != null) {
            type |= VIEW_TYPE_LAST_ROW;
        } else if (getGroupInfoAtPosition(position - 1) != null) {
            type |= VIEW_TYPE_FIRST_ROW;
        } else {
            type |= VIEW_TYPE_MID_ROW;
        }
        return type;
    }

    /**
     * when defining new view types just make sure to avoid bitwise-shifted 0x1
     * to keep the existing VIEW_TYPE_* items collision free
     *
     * @param position
     * @return
     */
    protected int getItemViewTypeInternal(final int position) {
        return super.getItemViewType(position);
    }

    protected boolean shouldDecorateItem(final int viewType) {
        return isArchViewType(viewType, VIEW_TYPE_MID_ROW | VIEW_TYPE_FIRST_ROW);
    }

    @NonNull
    protected ListItemViewHolder createListItemViewHolder(final View view, final int viewType) {
        return new ListItemViewHolder(view);
    }

    public final void notifyItemsGroupChanged(GroupInfo groupInfo){
        for (int i = 0; i < displayedGroups.size(); i++) {
            GroupInfoWrapper groupInfoWrapper = displayedGroups.get(displayedGroups.keyAt(i));
            if(groupInfoWrapper.getGroupInfo()==groupInfo){
                notifyItemRangeChanged(groupInfoWrapper.position, groupInfoWrapper.groupInfo.getItems().size());
                break;
            }
        }
    }

    protected void buildGroupedItemsData() {
        int count = hasHeader() && !headerDisplaysFirstLabel() ?
                1 :
                0;

        displayedGroups = new SparseArray<>();
        if (groupsInfo != null) {
            for (int i = 0; i < groupsInfo.size(); i++) {
                GroupInfo groupInfo = groupsInfo.get(i);

                int items = groupInfo.getItems().size();
                if (groupInfo.displayGroup() && items > 0) { // empty groups or groups not meant for display will be filtered out here
                    displayedGroups.append(count, new GroupInfoWrapper(groupInfo, count));
                    count += (items + 1); //+1 - for the label
                }
            }
        }
        mappedItems = new ArrayList<>();
        // add an empty item if there's a header and it's either
        // 1. independent of label or
        // 2. header shows the label but there are no items to display (it'll display the header with empty label)
        if (hasHeader() && (!headerDisplaysFirstLabel() || count == 0)) {
            mappedItems.add(null);
        }
        for (int i = 0; i < displayedGroups.size(); i++) {
            GroupInfoWrapper groupInfoWrapper = displayedGroups.get(displayedGroups.keyAt(i));

            mappedItems.add(null); // for the label
            mappedItems.addAll(groupInfoWrapper.getGroupInfo().getItems());
        }
        totalViews = mappedItems.size();
    }

    public interface GroupInfo<T> {

        CharSequence getLabel();

        List<? extends T> getItems();

        boolean displayGroup();

    }

    private class GroupInfoWrapper {

        private final GroupInfo groupInfo;


        private final int position;

        public GroupInfoWrapper(final GroupInfo groupInfo, final int position) {
            this.groupInfo = groupInfo;
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public GroupInfo getGroupInfo() {
            return groupInfo;
        }

    }

    public static class VaryingItemsDividerDecorator extends RecyclerView.ItemDecoration {

        public static final int KEY_DECORATION = R.id.recycler_view_item_decoration;

        public static final Object DECORATION_NONE = new Object();

        public static final Object DECORATION_TEXT_ALIGNED = new Object();

        public static final Object DECORATION_FULL_WIDTH = new Object();

        private final GroupedItemsAdapter itemsAdapter;

        Drawable divider;

        public VaryingItemsDividerDecorator(Context context, GroupedItemsAdapter itemsAdapter) {
            super();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                divider = context.getResources().getDrawable(R.drawable.default_list_item_divider, null);
            } else {
                divider = context.getResources().getDrawable(R.drawable.default_list_item_divider);
            }
            this.itemsAdapter = itemsAdapter;
        }

        @Override
        public void onDraw(final Canvas c, final RecyclerView parent, final RecyclerView.State state) {
            int children = parent.getChildCount();
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            for (int i = 0; i < children; i++) {
                View child = parent.getChildAt(i);
                if (itemsAdapter.isItemDecorated(child)) { // only items not decorated by the adapter. i.e header, footer and first/last row of a group
                    continue;
                }
                DecorationParams decorationParams = (DecorationParams) child.getTag(KEY_DECORATION);
                if (decorationParams ==null || decorationParams.type == DECORATION_NONE) {
                    continue;
                }

                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();


                int top = child.getBottom() + layoutParams.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();

                divider.setBounds(left + decorationParams.left, top, right + decorationParams.right, bottom);
                divider.draw(c);
            }

        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int bottom = itemsAdapter.isItemDecorated(view) ?
                    0 :
                    divider.getIntrinsicHeight();
            outRect.set(0, 0, 0, bottom);
        }
    }

}
