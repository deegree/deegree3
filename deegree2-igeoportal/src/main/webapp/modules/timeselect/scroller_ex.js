// CodeThatCalendar STANDARD
// Version: 3.2.1 (02.14.2006.1)
// THE SCRIPT IS FREE FOR NON-COMMERCIAL AND COMMERCIAL USE.
// Copyright (c) 2003-2006 by CodeThat.Com
// http://www.codethat.com/

var caldef1 = {
	firstday:0,
	//dtype:'dd/MM/yyyy',
	dtype:'yyyy-MM-dd',
	width:250,
	windoww:270,
	windowh:170,
	border_width:0,
	border_color:'#ffffff',
	dn_css:'clsDayName',
	cd_css:'clsCurrentDay',
	tw_css:'clsWorkDay',
	wd_css:'clsWorkDay',
	we_css:'clsWeekEnd',
	wdom_css:'clsWorkDayOtherMonth',
	weom_css:'clsWeekEndOtherMonth',
	wdomcw_css:'clsWorkDayOthMonthCurWeek',
	weomcw_css:'clsWeekEndOthMonthCurWeek',
	wecd_css:'clsWeekEndCurDay',
	wecw_css:'clsWeekEndCurWeek',
	highlight_css:'clsCurrentDay',
	headerstyle: {
	type : "comboboxes",
	css : 'clsWorkDayOtherMonth',
	yearrange : [1990, 2020]
	},
	monthnames :["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
	daynames: ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"],
	template_path:'',
	img_path: 'img/'
};