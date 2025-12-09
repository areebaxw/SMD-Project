import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.DrawerItem

class DrawerAdapter(
    private val items: List<DrawerItem>,
    private val listener: (DrawerItem) -> Unit
) : RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder>() {

    private var selectedPosition = -1

    inner class DrawerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view.findViewById(R.id.drawer_item_root)
        val icon: ImageView = view.findViewById(R.id.item_icon)
        val title: TextView = view.findViewById(R.id.item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.drawer_item, parent, false)
        return DrawerViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrawerViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.title.text = item.title

        // Set selected background and text color
        holder.root.isSelected = position == selectedPosition
        holder.title.setTextColor(
            if (position == selectedPosition) 0xFF8B2072.toInt() // opaque purple for selected
            else 0xFF000000.toInt() // black for unselected
        )

        holder.root.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            listener(item)
        }
    }



    override fun getItemCount(): Int = items.size
}
