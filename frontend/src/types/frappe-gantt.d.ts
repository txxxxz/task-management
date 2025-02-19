declare module 'frappe-gantt' {
  export default class Gantt {
    constructor(
      wrapper: HTMLElement,
      tasks: any[],
      options?: {
        header_height?: number;
        column_width?: number;
        step?: number;
        view_modes?: string[];
        bar_height?: number;
        bar_corner_radius?: number;
        arrow_curve?: number;
        padding?: number;
        view_mode?: string;
        date_format?: string;
        custom_popup_html?: any;
        language?: string;
        start_date?: Date;
        end_date?: Date;
        on_click?: (task: any) => void;
        on_date_change?: (task: any, start: string, end: string) => void;
        on_progress_change?: (task: any, progress: number) => void;
        on_view_change?: (mode: string) => void;
      }
    );
    change_view_mode(mode: string, start?: Date, end?: Date): void;
    refresh(tasks: any[]): void;
    start_date: Date;
    end_date: Date;
  }
} 