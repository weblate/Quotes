package phone.vishnu.quotes.fragment;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.ncorti.slidetoact.SlideToActView;

import java.io.File;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Objects;

import phone.vishnu.quotes.R;
import phone.vishnu.quotes.activity.SplashActivity;
import phone.vishnu.quotes.helper.AlarmHelper;
import phone.vishnu.quotes.helper.ExportHelper;
import phone.vishnu.quotes.helper.SharedPreferenceHelper;

public class SettingsFragment extends Fragment {

    private SwitchCompat reminderSwitch;
    private SlideToActView resetToggle;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private TextView shareActionPickTV;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferenceHelper = new SharedPreferenceHelper(Objects.requireNonNull(requireContext()));

        resetToggle = inflate.findViewById(R.id.settingsResetToggle);

        reminderSwitch = inflate.findViewById(R.id.settingsReminderSwitch);

        shareActionPickTV = inflate.findViewById(R.id.settingsShareActionPickTV);

        reminderSwitch.setChecked(!sharedPreferenceHelper.getAlarmString().equals("Alarm Not Set"));

        reminderSwitch.setText(getSwitchText(sharedPreferenceHelper.getAlarmString()));
        shareActionPickTV.setText(getSpannableText("Share", "What share button does"));

        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resetToggle.setOnSlideCompleteListener(slideToActView -> resetSettings(requireContext()));

        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                Calendar c = Calendar.getInstance();

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        requireContext(),
                        (view1, hourOfDay, minute) -> {

                            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            c.set(Calendar.MINUTE, minute);

                            sharedPreferenceHelper.setAlarmString("At " + hourOfDay + " : " + minute + " Daily");

                            reminderSwitch.setText(getSwitchText(MessageFormat.format("At {0} : {1} Daily", hourOfDay, minute)));

                            AlarmHelper.setAlarm(requireContext(), c);

                        },
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(requireContext())
                );

                timePickerDialog.setOnCancelListener(d -> reminderSwitch.setChecked(false));

                timePickerDialog.show();

            } else {
                alarmTurnedOff(requireContext());
            }
        });

        shareActionPickTV.setOnClickListener(v -> {
            ShareActionPickBottomSheetDialogFragment bottomSheet = ShareActionPickBottomSheetDialogFragment.newInstance();
            bottomSheet.show(requireActivity().getSupportFragmentManager(), "ModalBottomSheet");
        });
    }

    private void alarmTurnedOff(Context context) {
        reminderSwitch.setText(getSwitchText(""));

        sharedPreferenceHelper.setAlarmString("Alarm Not Set");

        AlarmHelper.cancelAlarm(context);
    }

    private void resetSettings(Context c) {

        sharedPreferenceHelper.resetSharedPreferences();

        deleteFiles(c);
    }

    private void deleteFiles(Context c) {
        ExportHelper exportHelper = new ExportHelper(c);

        File BGFile = new File(exportHelper.getBGPath());
        File SSFile = new File(exportHelper.getSSPath());

        if (BGFile.exists())
            BGFile.delete();
        if (SSFile.exists())
            SSFile.delete();

        requireActivity().finish();

        if (getContext() != null)
            requireActivity().startActivity(
                    new Intent(requireContext(), SplashActivity.class
                    ));

        Toast.makeText(requireContext(), "Settings Reset\nRestarting App for changes to take effect", Toast.LENGTH_SHORT).show();
    }

    private SpannableString getSwitchText(String v) {
        return getSpannableText("Daily Reminder", v);
    }

    private SpannableString getSpannableText(String s1, String s2) {

        String s = s1 + "\n" + s2;

        SpannableString spannableString = new SpannableString(s);
        spannableString.setSpan(new RelativeSizeSpan(1.5f), 0, s1.length(), 0);
        spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s1.length(), 0);

        return spannableString;

    }
}