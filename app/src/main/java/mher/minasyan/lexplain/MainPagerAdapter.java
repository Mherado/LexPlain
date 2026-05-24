package mher.minasyan.lexplain;

public class MainPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
    public MainPagerAdapter(@androidx.annotation.NonNull androidx.fragment.app.FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @androidx.annotation.NonNull
    @Override
    public androidx.fragment.app.Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new fragment_home();
            case 1:
                return new HistoryFragment();
            case 2:
                return new SettingsFragment();
            default:
                return new fragment_home();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}